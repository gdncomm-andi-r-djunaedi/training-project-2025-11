package com.blibli.gatewayModule.filter;

import com.blibli.gatewayModule.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();
        String method = request.getMethod().name();

        if (isPublicEndpoint(requestPath, method)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return sendUnauthorizedResponse(exchange);
        }

        Long memberId = jwtUtil.extractMemberId(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("Member-Id", String.valueOf(memberId))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicEndpoint(String path, String method) {
        if (path.startsWith("/api/products")) {
            return true;
        }
        if (path.startsWith("/api/members/register") && "POST".equals(method)) {
            return true;
        }
        if (path.startsWith("/api/members/login") && "POST".equals(method)) {
            return true;
        }
        if (path.startsWith("/api/members/logout") && "POST".equals(method)) {
            return true;
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> sendUnauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String errorBody = "{\"success\":false,\"errorMessage\":\"Invalid or missing token\",\"errorCode\":\"UNAUTHORIZED\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
    }
}