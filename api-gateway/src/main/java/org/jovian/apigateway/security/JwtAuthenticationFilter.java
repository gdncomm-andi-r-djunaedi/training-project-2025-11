package org.jovian.apigateway.security;

import lombok.RequiredArgsConstructor;
import org.jovian.apigateway.service.JwtService;
import org.jovian.apigateway.service.RedisTokenBlacklistService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final RedisTokenBlacklistService blacklistService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/auth/login") ||
                path.startsWith("/auth/register")
                ) {

            return chain.filter(exchange);
        }

        String token = jwtService.extractToken(exchange);

        if (token == null) {
            return jwtService.unauthorized(exchange, "Missing authentication token");
        }

        if (blacklistService.isBlacklisted(token)) {
            return jwtService.unauthorized(exchange, "Token is invalid (logged out)");
        }

        if (!jwtService.isTokenValid(token)) {
            return jwtService.unauthorized(exchange, "Invalid or expired token");
        }

        String userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);

        ServerWebExchange updatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .build())
                .build();

        return chain.filter(updatedExchange);

    }
    @Override
    public int getOrder() {
        return -1; // ensure this runs early
    }
}
