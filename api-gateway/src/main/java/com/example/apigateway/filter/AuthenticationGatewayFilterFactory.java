package com.example.apigateway.filter;

import com.example.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip authentication for open endpoints
            if (routeValidator.isSecured.test(request)) {
                // Extract token
                return jwtUtil.extractToken(request)
                        .map(token -> {
                            try {
                                // Validate token and extract claims
                                Claims claims = jwtUtil.validateToken(token);
                                String userId = claims.getSubject();

                                // Add user ID to request header for downstream services
                                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                        .header("X-User-Id", userId)
                                        .build();

                                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                            } catch (Exception e) {
                                log.error("Token validation failed: {}", e.getMessage());
                                ServerHttpResponse response = exchange.getResponse();
                                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                response.getHeaders().add("Content-Type", "application/json");
                                String errorMessage = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Invalid or missing token\"}";
                                DataBuffer buffer = response.bufferFactory()
                                        .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
                                return response.writeWith(Mono.just(buffer));
                            }
                        })
                        .orElseGet(() -> {
                            log.warn("Missing token for secured endpoint: {}", request.getURI().getPath());
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            response.getHeaders().add("Content-Type", "application/json");
                            String errorMessage = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Missing authorization header\"}";
                            DataBuffer buffer = response.bufferFactory()
                                    .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
                            return response.writeWith(Mono.just(buffer));
                        });
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}
