package com.project.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration
 * Provides KeyResolver for rate limiting based on IP address
 */
@Configuration
public class RateLimitConfig {

    /**
     * Default KeyResolver based on IP Address
     * Marked as @Primary to be used by default for RequestRateLimiter
     */
    @Bean(name = "ipKeyResolver")
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * KeyResolver based on User ID from JWT token
     * Falls back to IP if user ID is not available
     */
    @Bean(name = "userKeyResolver")
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (userId == null || userId.isEmpty()) {
                // Fallback to IP if no user ID
                String ip = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
                return Mono.just(ip);
            }
            
            return Mono.just(userId);
        };
    }
}

