package com.marketplace.gateway.filter;

import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.util.CookieUtil;
import com.marketplace.gateway.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global authentication filter for API Gateway.
 * Validates JWT tokens from Cookie (priority) or Authorization header.
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthFilter(JwtUtil jwtUtil, CookieUtil cookieUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Skip authentication for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extract token from Cookie or Authorization header
        String token = extractToken(exchange);
        if (token == null) {
            log.warn("Missing authentication token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Check if token is blacklisted
        return tokenBlacklistService.isBlacklisted(token)
            .flatMap(isBlacklisted -> {
                if (isBlacklisted) {
                    log.warn("Token is blacklisted for path: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                try {
                    jwtUtil.validateToken(token);
                } catch (Exception e) {
                    log.warn("Invalid token for path {}: {}", path, e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                return chain.filter(exchange);
            });
    }

    /**
     * Check if the path is public (no authentication required)
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/member/register") 
            || path.startsWith("/api/member/login")
            || path.startsWith("/api/auth/login")
            || path.startsWith("/api/auth/logout")
            || path.startsWith("/api/product/");  // Product browsing is public
    }

    /**
     * Extract JWT token from Cookie (priority) or Authorization header
     */
    private String extractToken(ServerWebExchange exchange) {
        // 1. Try to extract from Cookie first (more secure)
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieUtil.getAuthCookieName());
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
            log.debug("Token extracted from cookie");
            return cookie.getValue();
        }

        // 2. Fall back to Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token extracted from Authorization header");
            return authHeader.substring(7);
        }

        return null;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
