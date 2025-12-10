package com.marketplace.gateway.filter;

import com.marketplace.gateway.service.JwtService;
import com.marketplace.gateway.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/products"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.debug("Processing request to: {}", path);
        
        // Check if endpoint is public
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint, skipping authentication");
            return chain.filter(exchange);
        }
        
        // Extract token from header or cookie
        String token = extractToken(request);
        
        if (token == null) {
            log.error("No token found in request");
            return onError(exchange, "Missing authentication token", HttpStatus.UNAUTHORIZED);
        }
        
        // Validate token
        if (!jwtService.validateToken(token)) {
            log.error("Invalid JWT token");
            return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
        }
        
        // Check if token is blacklisted
        return tokenBlacklistService.isTokenBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.error("Token is blacklisted");
                        return onError(exchange, "Token has been invalidated", HttpStatus.UNAUTHORIZED);
                    }
                    
                    // Extract user information and add to headers
                    try {
                        Long userId = jwtService.extractUserId(token);
                        String email = jwtService.extractEmail(token);
                        String username = jwtService.extractUsername(token);
                        
                        // Add user context to request headers for downstream services
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", userId.toString())
                                .header("X-User-Email", email)
                                .header("X-User-Username", username)
                                .build();
                        
                        log.debug("Authentication successful for user: {}", userId);
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } catch (Exception e) {
                        log.error("Error extracting user info from token: {}", e.getMessage());
                        return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
                    }
                });
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private String extractToken(ServerHttpRequest request) {
        // Try to get token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try to get token from Cookie
        if (request.getCookies().containsKey("token")) {
            return request.getCookies().getFirst("token").getValue();
        }
        
        return null;
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorJson = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                status.getReasonPhrase(), message);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }
    
    @Override
    public int getOrder() {
        return -100;  // Execute before other filters
    }
}
