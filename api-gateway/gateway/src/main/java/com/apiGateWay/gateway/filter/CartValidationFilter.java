package com.apiGateWay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CartValidationFilter implements GatewayFilter {

    private final JwtTokenService jwtTokenService;

    public CartValidationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("CartValidationFilter: Validating token for {}", request.getPath());
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("CartValidationFilter: No Authorization header found");
            return unauthorizedResponse(exchange, "Missing Authorization header");
        }
        String token = jwtTokenService.extractTokenFromHeader(authHeader);
        if (token == null) {
            log.warn("CartValidationFilter: Invalid Authorization header format");
            return unauthorizedResponse(exchange, "Invalid Authorization header format");
        }
        JwtTokenService.ValidationResult result = jwtTokenService.validateWithContext(token);
        if (!result.isValid()) {
            log.warn("CartValidationFilter: Token validation failed");
            return unauthorizedResponse(exchange, "Invalid or expired token");
        }
        String email = result.getEmail();
        log.info("CartValidationFilter: Token validated successfully for user: {}", email);
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .build();
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String errorJson = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message);
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
