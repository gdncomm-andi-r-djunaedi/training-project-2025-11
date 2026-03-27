package com.ecom.gateway2.controller.filter;

import com.ecom.gateway2.controller.controller.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authRequired = exchange.getRequest().getQueryParams().getFirst("authRequired");
        boolean isAuthRequired = "true".equalsIgnoreCase(authRequired);

        if (!isAuthRequired) {
            log.info("Inside No auth validation check");
            return chain.filter(exchange);
        }

        return validateAndProcessCartRequest(exchange, chain);

    }

    private Mono<Void> validateAndProcessCartRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header for cart API");
            return unauthorized(exchange, "Missing token. Please login again.");
        }

        String token = authHeader.substring(7);
        String userId;

        try {
            jwtUtil.validateToken(token);

            userId = jwtUtil.extractUserId(token);
            log.info("Token validated successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return unauthorized(exchange, "Invalid session. Please login again.");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(message.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }


    @Override
    public int getOrder() {
        return 0;
    }
}

