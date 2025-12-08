package com.blibi.apigateway.filter;

import com.blibi.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter to extract userId from JWT token and add as header
 * for downstream services (specifically Cart Service).
 * 
 * This filter:
 * 1. Intercepts requests to /api/cart/** routes
 * 2. Extracts JWT token from Authorization header
 * 3. Validates token and extracts userId claim
 * 4. Adds X-User-Id header to forwarded request
 * 
 * This eliminates the need for clients to pass userId in path parameters,
 * improving security and API design.
 * 
 * Topics to be learned: Spring Cloud Gateway Filters, Reactive Programming
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserIdExtractionFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    /**
     * Filter logic to extract userId from JWT and add as header
     * 
     * @param exchange Current server exchange
     * @param chain    Gateway filter chain
     * @return Mono to indicate when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Only apply to cart routes
        if (!path.startsWith("/api/cart")) {
            return chain.filter(exchange);
        }

        log.debug("Applying UserIdExtractionFilter for path: {}", path);

        try {
            // Extract token from Authorization header
            String token = extractToken(exchange);

            if (token != null) {
                // Validate token and extract claims
                Claims claims = jwtUtil.validateAndGetClaims(token);
                String userId = claims.get("userId", String.class);

                if (userId != null) {
                    log.debug("Extracted userId from token: {}", userId);

                    // Add userId as header for downstream services
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userId)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    log.warn("userId claim not found in JWT token");
                }
            } else {
                log.warn("No authorization token found for cart request");
            }
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
        }

        // Continue without adding header if extraction fails
        return chain.filter(exchange);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * @param exchange Current server exchange
     * @return JWT token string or null if not found
     */
    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    /**
     * Set filter order to execute before routing
     * Lower values have higher priority
     * 
     * @return Order value (-100 to execute early in filter chain)
     */
    @Override
    public int getOrder() {
        return -100; // Execute before routing
    }
}
