package com.project.gateway.filter;

import com.project.gateway.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global JWT Authentication Filter
 * Validates JWT tokens for protected routes
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final List<String> publicPaths;

    @Autowired
    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Value("${gateway.public-paths:}") String publicPathsStr) {
        this.jwtUtil = jwtUtil;
        // Parse comma-separated public paths
        if (publicPathsStr != null && !publicPathsStr.trim().isEmpty()) {
            this.publicPaths = List.of(publicPathsStr.split(","));
        } else {
            this.publicPaths = List.of();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Check if path is public (no authentication required)
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null || token.isEmpty()) {
            return handleUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            return handleUnauthorized(exchange, "Invalid or expired token");
        }

        try {
            // Extract user information from token
            String username = jwtUtil.extractUsername(token);
            String userId = jwtUtil.extractUserId(token);
            
            // If userId is not in token, use username (email) as userId
            if (userId == null || userId.isEmpty()) {
                userId = username;
            }

            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Username", username != null ? username : "")
                    .build();

            log.debug("JWT validated successfully for user: {} (userId: {}) on path: {}", username, userId, path);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            return handleUnauthorized(exchange, "Error processing token: " + e.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .map(String::trim)
                .anyMatch(publicPath -> {
                    // Remove wildcards for matching
                    String cleanPath = publicPath.replace("**", "").replace("*", "");
                    return path.startsWith(cleanPath);
                });
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorBody = String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now(),
                message,
                exchange.getRequest().getURI().getPath()
        );

        log.warn("Unauthorized access attempt: {} - Path: {}", message, exchange.getRequest().getURI().getPath());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // High priority to execute early
    }
}

