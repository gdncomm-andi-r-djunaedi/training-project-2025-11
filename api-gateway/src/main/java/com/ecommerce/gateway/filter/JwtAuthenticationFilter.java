package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.service.JwtService;
import com.ecommerce.gateway.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @Override
    public int getOrder() {
        // Run before Spring Security filters
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip filter for public endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extract JWT from Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token, let Spring Security handle it
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        // Check if token is blacklisted
        return tokenBlacklistService.isBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Blacklisted token attempted to access: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // Token is not blacklisted, decode and add userId header
                    try {
                        Jwt jwt = jwtService.decodeToken(token);
                        Object userIdObj = jwt.getClaims().get("userId");
                        String userId = userIdObj != null ? userIdObj.toString() : "";

                        if (!userId.isEmpty()) {
                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .build();
                            return chain.filter(exchange.mutate().request(request).build());
                        }
                    } catch (Exception e) {
                        log.error("Error decoding JWT: {}", e.getMessage());
                        // Let Spring Security handle invalid tokens
                    }

                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Error checking token blacklist: {}", e.getMessage());
                    // On error, let the request continue (fail open for availability)
                    return chain.filter(exchange);
                });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/gateway/auth/")
                || path.equals("/api/members/login")
                || path.equals("/api/members/register")
                || path.startsWith("/api/products/");
    }
}
