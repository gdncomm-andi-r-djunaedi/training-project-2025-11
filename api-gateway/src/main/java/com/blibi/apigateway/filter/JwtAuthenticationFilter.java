package com.blibi.apigateway.filter;
import com.blibi.apigateway.configuration.JwtProperties;
import com.blibi.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT authentication filter for validating tokens on incoming requests
 * Supports both Bearer token and cookie-based authentication
 * Validates token against Redis blacklist and checks expiration
 * 
 * Topics to be learned: WebFilter, Reactive Programming, JWT Validation, Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for login endpoint and member registration
        if (path.startsWith("/auth/login") || path.startsWith("/api/member/register")) {
            return chain.filter(exchange);
        }

        // Extract token from Authorization header or cookie
        String token = extractToken(exchange);

        if (token == null) {
            log.warn("No token found in request to: {}", path);
            return unauthorized(exchange);
        }

        // Check if token is blacklisted in Redis
        if (Boolean.TRUE.equals(redisTemplate.hasKey("invalid:" + token))) {
            log.warn("Token is blacklisted: {}", token.substring(0, Math.min(20, token.length())));
            return unauthorized(exchange);
        }

        // Validate token and check expiration
        try {
            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Token is expired");
                return unauthorized(exchange);
            }

            // Validate token signature and claims
            String username = jwtUtil.validateToken(token);
            log.debug("Token validated successfully for user: {}", username);

            // Add username to exchange attributes for downstream use
            exchange.getAttributes().put("username", username);

        } catch (Exception ex) {
            log.error("Token validation failed", ex);
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    /**
     * Extract JWT token from Authorization header or cookie
     * 
     * @param exchange Server web exchange
     * @return JWT token string or null if not found
     */
    private String extractToken(ServerWebExchange exchange) {
        // Try Authorization header first
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }

        // Fallback to cookie
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get(jwtProperties.getCookieName());
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).getValue();
        }

        return null;
    }

    /**
     * Return unauthorized response
     * 
     * @param exchange Server web exchange
     * @return Mono<Void> with 401 status
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
