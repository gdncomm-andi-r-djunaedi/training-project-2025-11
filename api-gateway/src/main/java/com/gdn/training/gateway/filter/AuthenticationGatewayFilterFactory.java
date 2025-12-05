package com.gdn.training.gateway.filter;

import com.gdn.training.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    System.out.println("invalid access...!" + e.getMessage());
                    throw new RuntimeException("unauthorized access to application");
                }

                String finalAuthHeader = authHeader;

                return redisTemplate.hasKey(finalAuthHeader)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }

                            // Extract username and add to header
                            io.jsonwebtoken.Claims claims = jwtUtil.extractAllClaims(finalAuthHeader);
                            String username = claims.getSubject();
                            String memberId = String.valueOf(claims.get("memberId"));

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Name", username)
                                    .header("X-Member-Id", memberId)
                                    .build();

                            return chain.filter(exchange.mutate().request(request).build());
                        });
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
