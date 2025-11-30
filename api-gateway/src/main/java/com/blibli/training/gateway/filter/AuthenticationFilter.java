package com.blibli.training.gateway.filter;

import com.blibli.training.framework.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/auth/") || path.startsWith("/member/register") || path.startsWith("/member/login")) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());
        if (token == null || !jwtUtils.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = jwtUtils.getUserIdFromToken(token);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    private String extractToken(ServerHttpRequest request) {
        // Check Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check Cookie
        HttpCookie cookie = request.getCookies().getFirst("token");
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
