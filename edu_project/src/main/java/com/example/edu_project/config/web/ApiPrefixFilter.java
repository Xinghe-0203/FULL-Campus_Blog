package com.example.edu_project.config.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Exposes the SPA at the server root while keeping the public API under /api.
 * The servlet context itself stays at / so the JAR can be opened at the domain root.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "api.root-prefix.enabled", havingValue = "true", matchIfMissing = true)
public class ApiPrefixFilter extends OncePerRequestFilter {

    public static final String API_REQUEST_ATTRIBUTE = ApiPrefixFilter.class.getName() + ".apiRequest";
    public static final String ORIGINAL_URI_ATTRIBUTE = ApiPrefixFilter.class.getName() + ".originalUri";
    private static final String API_PREFIX = "/api";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(Math.min(contextPath.length(), requestUri.length()));

        if (path.equals(API_PREFIX) || path.equals(API_PREFIX + "/")) {
            response.sendRedirect(contextPath + "/");
            return;
        }

        if (path.equals(API_PREFIX + "/app") || path.equals(API_PREFIX + "/app/")) {
            response.sendRedirect(contextPath + "/");
            return;
        }

        if (path.startsWith(API_PREFIX + "/")) {
            String applicationPath = path.substring(API_PREFIX.length());
            request.setAttribute(API_REQUEST_ATTRIBUTE, Boolean.TRUE);
            request.setAttribute(ORIGINAL_URI_ATTRIBUTE, requestUri);
            filterChain.doFilter(new ApiPathRequestWrapper(request, applicationPath), response);
            return;
        }

        if ((HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod()))
                && isSpaRoute(path)) {
            request.getRequestDispatcher(contextPath + "/index.html").forward(request, response);
            return;
        }

        if (!HttpMethod.GET.matches(request.getMethod())
                && !HttpMethod.HEAD.matches(request.getMethod())
                && !HttpMethod.OPTIONS.matches(request.getMethod())
                && !path.startsWith("/uploads/")) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    public static boolean isApiRequest(HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getAttribute(API_REQUEST_ATTRIBUTE));
    }

    private boolean isSpaRoute(String path) {
        return !isPathOrChild(path, "/uploads")
                && !isPathOrChild(path, "/assets")
                && !isPathOrChild(path, "/actuator")
                && !isPathOrChild(path, "/swagger-ui")
                && !isPathOrChild(path, "/swagger-resources")
                && !isPathOrChild(path, "/v3/api-docs")
                && !path.equals("/index.html")
                && !path.equals("/doc.html")
                && !path.equals("/favicon.svg")
                && !path.equals("/default-avatar.png")
                && !path.equals("/default-cover.jpg")
                && !path.equals("/default-image.png")
                && (path.equals("/") || !path.substring(path.lastIndexOf('/') + 1).contains("."));
    }

    private boolean isPathOrChild(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private static final class ApiPathRequestWrapper extends HttpServletRequestWrapper {

        private final String applicationPath;

        private ApiPathRequestWrapper(HttpServletRequest request, String applicationPath) {
            super(request);
            this.applicationPath = applicationPath;
        }

        @Override
        public String getRequestURI() {
            return applicationPath;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer originalUrl = super.getRequestURL();
            String originalUri = super.getRequestURI();
            int pathStart = originalUrl.length() - originalUri.length();
            return new StringBuffer(originalUrl.substring(0, pathStart)).append(applicationPath);
        }

        @Override
        public String getServletPath() {
            return applicationPath;
        }

        @Override
        public String getPathInfo() {
            return null;
        }
    }
}
