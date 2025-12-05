package com.gdn.gateway.utils;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthFilter(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getPath().value();
            log.info("Incoming request path: {}", path);

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return unauthorized(exchange, "Missing Authorization header");
            }

            String token = authHeader.substring(7);
            log.debug("Extracted token: {}", token);

            Claims claims;
            try {
                claims = jwtUtil.validate(token);
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
                return unauthorized(exchange, "Invalid or expired token");
            }

            String emailFromToken = claims.getSubject();
            log.info("Token subject (email): {}", emailFromToken);

            String redisKey = token + ":" + emailFromToken;
            Boolean exists = redisTemplate.hasKey(redisKey);
            log.debug("Redis lookup key={}, exists={}", redisKey, exists);

            if (!Boolean.TRUE.equals(exists)) {
                log.warn("No active token in Redis for email={}, token={}", emailFromToken, token);
                return unauthorized(exchange, "Token revoked. Please login again.");
            }

            String emailFromQuery = exchange.getRequest()
                    .getQueryParams()
                    .getFirst("memberId");

            if (emailFromQuery == null || emailFromQuery.isBlank()) {
                log.warn("memberId query param missing");
                return unauthorized(exchange, "memberId missing");
            }

            if (!emailFromToken.equalsIgnoreCase(emailFromQuery)) {
                log.warn("memberId mismatch. tokenEmail={}, queryEmail={}",
                        emailFromToken, emailFromQuery);
                return unauthorized(exchange, "memberId (email) mismatch");
            }

            log.info("JWT + Redis + memberId validation PASSED for email={}", emailFromToken);

            return chain.filter(exchange);
        };

}


    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        log.error("Unauthorized request: {}", msg);

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String json = "{\"error\": \"" + msg + "\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        );
    }

    public static class Config {}
}
