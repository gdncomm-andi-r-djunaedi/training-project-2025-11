package com.gdn.faurihakim.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /**
     * Key resolver for rate limiting.
     * Uses user ID from X-User-Id header for authenticated requests,
     * falls back to IP address for unauthenticated requests.
     * 
     * This bean is only created when rate.limit.enabled=true
     * To disable rate limiting, set: rate.limit.enabled=false in
     * application.properties
     */
    @Bean
    @ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true", matchIfMissing = false)
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from header (set by AuthenticationFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }

            // Fall back to IP address for unauthenticated requests
            String ipAddress = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ipAddress);
        };
    }
}
