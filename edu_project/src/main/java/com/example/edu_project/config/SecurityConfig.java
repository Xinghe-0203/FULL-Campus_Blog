package com.example.edu_project.config;

import com.example.edu_project.config.JwtAuthenticationFilter;
import com.example.edu_project.utils.LogUtils;
import com.example.edu_project.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * CORS 允许的来源列表
     * 可通过环境变量 CORS_ALLOWED_ORIGINS 配置，多个来源用逗号分隔
     * 示例: http://localhost:8080,http://127.0.0.1:8080,https://campus-blog.com
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8825,http://127.0.0.1:3000,http://127.0.0.1:8825}")
    private String allowedOrigins;

    @Value("${bcrypt.strength:12}")
    private int bcryptStrength;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        List<String> validatedOrigins = new ArrayList<>();
        for (String origin : origins) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.endsWith("*")) {
                    log.warn("CORS 配置包含通配符，生产环境请使用具体来源: {}", trimmed);
                }
                configuration.addAllowedOriginPattern(trimmed);
            }
        }
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("Refresh-Token");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("X-Trace-Id");
        configuration.addAllowedHeader("X-XSRF-TOKEN");
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.PATCH);
        configuration.addAllowedMethod(HttpMethod.OPTIONS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 记录管理员操作审计日志
     */
    private void logAdminOperation(String action, HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        String clientIp = LogUtils.getClientIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        LogUtils.logSecurityEvent("ADMIN_OPERATION",
                "action=" + action + ", uri=" + uri + ", method=" + method + ", userId=" + (userId != null ? userId : "anonymous"),
                clientIp);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disabled原因: 本系统使用JWT Bearer Token认证，token通过Authorization header传递
            // 浏览器同源策略阻止跨域网站读取Authorization header，因此CSRF攻击不可行
            // 如未来改用Cookie认证，需重新启用CSRF保护
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    String json = "{\"code\":401,\"message\":\"认证失败，请先登录\"}";
                    response.getWriter().write(json);
                    response.getWriter().flush();
                    LogUtils.logSecurityEvent("AUTH_ENTRY_POINT", authException.getMessage(),
                            LogUtils.getClientIp(request));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    String json = "{\"code\":403,\"message\":\"权限不足，拒绝访问\"}";
                    response.getWriter().write(json);
                    response.getWriter().flush();
                    LogUtils.logSecurityEvent("ACCESS_DENIED", accessDeniedException.getMessage(),
                            LogUtils.getClientIp(request));
                })
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error", "/error/**").permitAll()
                .requestMatchers("/user/register", "/user/login", "/user/refresh", "/auth/register/send-code", "/auth/register/verify", "/auth/password/send-code", "/auth/password/reset-password").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers("/admin/**").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/post/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/post/list").permitAll()
                .requestMatchers(HttpMethod.GET, "/post/search/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/post/{id}/view").permitAll()
                .requestMatchers(HttpMethod.GET, "/comment/post/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/comment/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/like/*/status").permitAll()
                .requestMatchers(HttpMethod.GET, "/collect/*/status").permitAll()
                .requestMatchers(HttpMethod.GET, "/tag/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/follow/*/status").permitAll()
                .requestMatchers(HttpMethod.GET, "/trending/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/feed/recommend").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/post/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/like/check/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/comment/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/circle/user/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/share/count/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/follow/followers/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/follow/following/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/follow/counts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/topic/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/statistics/community").permitAll()
                .requestMatchers(HttpMethod.GET, "/media/post/**").permitAll()
                .requestMatchers("/follow/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/post/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/post/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/post/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}