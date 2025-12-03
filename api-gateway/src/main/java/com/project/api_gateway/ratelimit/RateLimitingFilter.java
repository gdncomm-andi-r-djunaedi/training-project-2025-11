package com.project.api_gateway.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${rate.limit.capacity:60}")
    private long capacity;

    @Value("${rate.limit.refill.period:1}")
    private long refillPeriod;

    @Value("${rate.limit.refill.unit:MINUTES}")
    private String refillUnit;

    private Bucket newBucket() {
        Duration period = switch (refillUnit.toUpperCase()) {
            case "SECONDS" -> Duration.ofSeconds(refillPeriod);
            case "HOURS" -> Duration.ofHours(refillPeriod);
            case "DAYS" -> Duration.ofDays(refillPeriod);
            default -> Duration.ofMinutes(refillPeriod);
        };
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, period));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Allow preflight and public paths without rate limiting
        if (request.getMethod() == HttpMethod.OPTIONS ||
                path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.equals("/swagger-ui.html") ||
                path.startsWith("/api/member/login") || path.startsWith("/api/member/register")) {
            return chain.filter(exchange);
        }

        String key = request.getHeaders().getFirst("X-User-Subject");
        if (key == null || key.isBlank()) {
            String xff = request.getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                key = xff.split(",")[0].trim();
            } else {
                InetSocketAddress remote = request.getRemoteAddress();
                key = remote != null ? remote.getAddress().getHostAddress() : "unknown";
            }
        }

        Bucket bucket = cache.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Apply rate limiting after CORS but before routing
        return -1;
    }
}