package com.example.edu_project.utils;

import com.example.edu_project.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * JWT 工具类
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Token黑名单：存储已撤销的Token
     * 生产环境应使用 Redis 实现分布式 Token 黑名单
     * 使用 LinkedBlockingQueue 实现有界队列，队满时自动淘汰最老的条目
     */
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    /**
     * 用户设备会话映射: userId -> deviceTokens (CopyOnWriteArraySet for concurrent iteration)
     * 用于支持选择性登出特定设备
     */
    private final Map<Long, Set<String>> userDeviceTokens = new ConcurrentHashMap<>();

    /**
     * 黑名单最大容量，防止内存无限增长
     * 队满时清理已过期的token后再添加
     */
    private static final int BLACKLIST_MAX_SIZE = 100_000;

    private static final int MAX_DEVICES_PER_USER = 10;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT密钥长度必须至少32个字符");
        }
    }

    /**
     * 生成 Token（包含角色）
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return createToken(claims, username, expiration);
    }

    /**
     * 生成刷新Token（用于获取新的访问Token）
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "refresh"); // 标记为刷新Token
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * 注册用户设备令牌（用于多设备管理和选择性登出）
     * @param userId 用户ID
     * @param token 访问令牌
     */
    public void registerDeviceToken(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        Set<String> devices = userDeviceTokens.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        synchronized (devices) {
            devices.add(token);
            while (devices.size() > MAX_DEVICES_PER_USER) {
                String oldestToken = devices.iterator().next();
                devices.remove(oldestToken);
                tokenBlacklist.add(oldestToken);
            }
        }
    }

    /**
     * 登出指定设备
     * @param userId 用户ID
     * @param token 要登出的设备令牌
     */
    public void logoutDevice(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        Set<String> devices = userDeviceTokens.get(userId);
        if (devices != null) {
            devices.remove(token);
        }
        tokenBlacklist.add(token);
        log.info("设备登出: userId={}, deviceToken={}", userId, token.substring(0, Math.min(20, token.length())));
    }

    /**
     * 登出除指定令牌外的所有设备
     * @param userId 用户ID
     * @param excludeToken 保留的令牌（当前设备）
     */
    public void logoutOtherDevices(Long userId, String excludeToken) {
        if (userId == null) {
            return;
        }
        Set<String> devices = userDeviceTokens.get(userId);
        if (devices != null) {
            for (String deviceToken : devices) {
                if (!deviceToken.equals(excludeToken)) {
                    tokenBlacklist.add(deviceToken);
                }
            }
            devices.clear();
            if (excludeToken != null) {
                devices.add(excludeToken);
            }
        }
        log.info("其他设备已登出: userId={}", userId);
    }

    /**
     * 获取用户设备数量
     * @param userId 用户ID
     * @return 设备数量
     */
    public int getUserDeviceCount(Long userId) {
        Set<String> devices = userDeviceTokens.get(userId);
        return devices != null ? devices.size() : 0;
    }

    /**
     * 验证刷新Token是否有效
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * 将Token加入黑名单（撤销Token）
     */
    public void revokeToken(String token) {
        if (token == null) {
            return;
        }
        try {
            parseToken(token);
            if (isTokenExpired(token)) {
                return;
            }
            synchronized (tokenBlacklist) {
                if (tokenBlacklist.size() >= BLACKLIST_MAX_SIZE) {
                    cleanExpiredTokens();
                }
                if (tokenBlacklist.size() < BLACKLIST_MAX_SIZE) {
                    tokenBlacklist.add(token);
                }
            }
        } catch (Exception e) {
            log.warn("撤销token失败: {}", e.getMessage());
        }
    }

    /**
     * 检查Token是否已被撤销
     */
    public boolean isTokenRevoked(String token) {
        return tokenBlacklist.contains(token);
    }

    /**
     * 清理过期Token黑名单
     * 注意：生产环境应使用Redis并设置TTL自动过期
     */
    public void cleanExpiredTokens() {
        tokenBlacklist.removeIf(token -> {
            try {
                return isTokenExpired(token);
            } catch (Exception e) {
                // 无效Token直接移除
                return true;
            }
        });
    }

    /**
     * 从请求中提取 Token
     * @param request HTTP请求
     * @return Token字符串，如果不存在返回null
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true; // 已过期
        } catch (io.jsonwebtoken.JwtException e) {
            // 其他 JWT 解析错误（格式错误、签名验证失败等）不算过期，而是无效
            // 这里返回 false 让调用方通过 parseToken 的结果判断
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            throw new BusinessException(401, "无效的Token");
        }
        return userId;
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取用户角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
