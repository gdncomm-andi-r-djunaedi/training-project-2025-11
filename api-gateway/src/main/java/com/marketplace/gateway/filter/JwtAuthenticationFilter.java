package com.marketplace.gateway.filter;

import com.marketplace.common.constants.AppConstants;
import com.marketplace.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * API Gateway JWT Authentication Filter.
 * 
 * This filter validates JWT tokens at the gateway level and passes them through
 * to downstream services. Downstream services will perform their own validation
 * using the same RSA public key.
 * 
 * The gateway provides an initial validation layer for early rejection of invalid tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/members/register",
            "/api/members/login",
            "/api/products",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Check if it's a public endpoint
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Get token from header
        String authHeader = request.getHeaders().getFirst(AppConstants.JWT_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_PREFIX)) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(AppConstants.JWT_PREFIX.length());

        // Check if token is blacklisted
        return isTokenBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        return onError(exchange, "Token has been invalidated", HttpStatus.UNAUTHORIZED);
                    }

                    // Validate token at gateway level for early rejection
                    if (!jwtTokenProvider.validateToken(token)) {
                        return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                    }

                    // Pass the request through - downstream services will validate the token themselves
                    // The Authorization header is already present and will be forwarded
                    log.debug("JWT validated at gateway, forwarding request to: {}", path);
                    return chain.filter(exchange);
                });
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Boolean> isTokenBlacklisted(String token) {
        String key = AppConstants.TOKEN_BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(key);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.error("Authentication error: {}", message);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
