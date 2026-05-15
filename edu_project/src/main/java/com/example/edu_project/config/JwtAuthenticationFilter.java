package com.example.edu_project.config;

import com.example.edu_project.utils.JwtUtils;
import com.example.edu_project.utils.LogUtils;
import com.example.edu_project.utils.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JWT 认证过滤器
 * 每次请求都检查 Authorization Header 中的 Token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 初始化MDC上下文
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        LogUtils.initMdc();

        try {
            String token = jwtUtils.extractTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                try {
                    // 拒绝 refresh token 用于 API 访问，防止泄露后长期滥用
                    if (jwtUtils.isRefreshToken(token)) {
                        // Refresh token 只能用于 /user/refresh 端点，其他接口直接返回 401
                        String uri = request.getRequestURI();
                        if (uri.endsWith("/user/refresh") || uri.equals("/api/user/refresh")) {
                            log.debug("Refresh token allowed on /user/refresh endpoint");
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":401,\"message\":\"Refresh token cannot be used for API access\"}");
                            LogUtils.logSecurityEvent("REFUSED_REFRESH_TOKEN",
                                    "Refresh token used for API access",
                                    LogUtils.getClientIp(request));
                            return;
                        }
                    }
                    // 必须先验证 Token 签名，再检查黑名单
                    if (!jwtUtils.isTokenExpired(token) && !jwtUtils.isTokenRevoked(token)) {
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        String username = jwtUtils.getUsernameFromToken(token);
                        String role = jwtUtils.getRoleFromToken(token);

                        // 设置用户上下文到MDC
                        LogUtils.setUserContext(userId, username);

                        // 创建用户上下文对象
                        UserContext userContext = new UserContext(userId, role);

                        // 创建认证对象，设置正确的权限列表
                        List<GrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority(role != null ? "ROLE_" + role : "ROLE_USER")
                        );
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userContext, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 将认证信息存入 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT Authentication successful for user: {}", username);
                    } else {
                        log.debug("JWT token expired or revoked");
                    }
                } catch (Exception e) {
                    // Token 无效或解析失败，继续过滤链（未认证）
                    log.warn("JWT Authentication failed: {}", e.getMessage());
                    LogUtils.logSecurityEvent("JWT_AUTH_FAILED", e.getMessage(),
                            LogUtils.getClientIp(request));
                }
            }
        } catch (Exception e) {
            // Token 无效或解析失败，继续过滤链（未认证）
            log.warn("JWT Authentication failed: {}", e.getMessage());
            LogUtils.logSecurityEvent("JWT_FILTER_ERROR", e.getMessage(),
                    LogUtils.getClientIp(request));
        }

        filterChain.doFilter(request, response);
    }
}
