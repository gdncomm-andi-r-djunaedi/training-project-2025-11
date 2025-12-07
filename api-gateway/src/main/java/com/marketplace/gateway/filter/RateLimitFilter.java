package com.marketplace.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

/**
 * Rate limiting filter using Redis to prevent API abuse.
 * Implements a sliding window rate limiter per IP address.
 * <p>
 * Configuration properties:
 * - ratelimit.requests-per-minute: Max requests per minute (default: 100)
 * - ratelimit.enabled: Enable/disable rate limiting (default: true)
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${ratelimit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${ratelimit.enabled:true}")
    private boolean enabled;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange);
        String key = RATE_LIMIT_KEY_PREFIX + clientIp;

        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // First request, set expiry
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > requestsPerMinute) {
                        log.warn("Rate limit exceeded for IP: {} (count: {})", clientIp, count);

                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("X-Rate-Limit-Limit",
                                String.valueOf(requestsPerMinute));
                        exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", "0");
                        exchange.getResponse().getHeaders().add("Retry-After", "60");

                        return exchange.getResponse().setComplete();
                    }

                    // Add rate limit headers for transparency
                    long remaining = Math.max(0, requestsPerMinute - count);
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Limit", String.valueOf(requestsPerMinute));
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", String.valueOf(remaining));

                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    // If Redis is unavailable, allow the request but log the error
                    log.error("Rate limiting unavailable (Redis error): {}", e.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * Extract client IP address, considering proxy headers.
     */
    private String getClientIp(ServerWebExchange exchange) {
        // Check for X-Forwarded-For header (for load balancers/proxies)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (client's real IP)
            return xForwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fall back to remote address
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse("unknown");
    }

    @Override
    public int getOrder() {
        // Run before AuthFilter
        return -2;
    }
}
