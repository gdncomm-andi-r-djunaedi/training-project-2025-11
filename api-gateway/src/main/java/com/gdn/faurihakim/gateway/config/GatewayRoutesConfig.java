package com.gdn.faurihakim.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${member.service.url}")
    private String memberServiceUrl;

    /**
     * Configure routes with rate limiting when enabled.
     */
    @Bean
    @ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true")
    public RouteLocator rateLimitedRoutes(RouteLocatorBuilder builder, KeyResolver userKeyResolver) {
        return builder.routes()
                // Member Service with rate limiting (10 req/s, burst 20)
                .route("member-service", r -> r
                        .path("/api/members/**")
                        .filters(f -> f.requestRateLimiter(c -> c
                                .setRateLimiter(new RedisRateLimiter(10, 20))
                                .setKeyResolver(userKeyResolver)))
                        .uri(memberServiceUrl))

                // Product Service with rate limiting (20 req/s, burst 40)
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f.requestRateLimiter(c -> c
                                .setRateLimiter(new RedisRateLimiter(20, 40))
                                .setKeyResolver(userKeyResolver)))
                        .uri("http://localhost:8082"))

                // Cart Service with rate limiting (15 req/s, burst 30)
                .route("cart-service", r -> r
                        .path("/api/carts/**")
                        .filters(f -> f.requestRateLimiter(c -> c
                                .setRateLimiter(new RedisRateLimiter(15, 30))
                                .setKeyResolver(userKeyResolver)))
                        .uri("http://localhost:8083"))
                .build();
    }

    /**
     * Default routes without rate limiting when disabled.
     */
    @Bean
    @ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "false", matchIfMissing = true)
    public RouteLocator defaultRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("member-service-default", r -> r
                        .path("/api/members/**")
                        .uri(memberServiceUrl))
                .route("product-service-default", r -> r
                        .path("/api/products/**")
                        .uri("http://localhost:8082"))
                .route("cart-service-default", r -> r
                        .path("/api/carts/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}
