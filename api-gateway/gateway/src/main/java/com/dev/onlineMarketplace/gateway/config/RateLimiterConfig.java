package com.dev.onlineMarketplace.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rate Limiter Configuration
 * 
 * Provides different strategies for rate limiting:
 * - IP-based: Limits requests per IP address
 * - User-based: Limits requests per authenticated user
 * - Path-based: Limits requests per endpoint
 * - Combined: Combines multiple strategies
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Primary KeyResolver - Combines IP and User information
     * 
     * For authenticated users: Uses username from JWT
     * For anonymous users: Uses IP address
     * 
     * This provides fair rate limiting for both authenticated and public endpoints
     */
    @Primary
    @Bean
    public KeyResolver combinedKeyResolver() {
        return exchange -> {
            // Try to get username from JWT token
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (username != null && !username.isEmpty()) {
                // For authenticated requests, limit by username
                return Mono.just("user:" + username);
            }
            
            // For anonymous requests, limit by IP address
            String ipAddress = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()
            ).getAddress().getHostAddress();
            
            return Mono.just("ip:" + ipAddress);
        };
    }

    /**
     * IP-based KeyResolver
     * Limits requests based on client IP address
     * Good for public endpoints or preventing abuse
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ipAddress = Objects.requireNonNull(
                    exchange.getRequest().getRemoteAddress()
            ).getAddress().getHostAddress();
            return Mono.just(ipAddress);
        };
    }

    /**
     * User-based KeyResolver
     * Limits requests based on authenticated user
     * Requires X-User-Id header to be set by authentication filter
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(username != null ? username : "anonymous");
        };
    }

    /**
     * Path-based KeyResolver
     * Limits requests based on the request path
     * Useful for protecting specific endpoints
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

    /**
     * API Key-based KeyResolver
     * Limits requests based on API key in header
     * Useful for third-party API consumers
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            return Mono.just(apiKey != null ? apiKey : "no-api-key");
        };
    }
}

