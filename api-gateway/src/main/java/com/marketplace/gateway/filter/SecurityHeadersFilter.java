package com.marketplace.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that adds security headers to all responses.
 * These headers help protect against common web vulnerabilities.
 */
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();

            // Prevent MIME type sniffing
            headers.add("X-Content-Type-Options", "nosniff");

            // Prevent clickjacking
            headers.add("X-Frame-Options", "DENY");

            // Enable XSS filter in browsers
            headers.add("X-XSS-Protection", "1; mode=block");

            // Enforce HTTPS (comment out if not using HTTPS in development)
            // headers.add("Strict-Transport-Security", "max-age=31536000;
            // includeSubDomains");

            // Referrer policy for privacy
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions policy (formerly Feature-Policy)
            headers.add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

            // Cache control for sensitive data
            if (exchange.getRequest().getURI().getPath().contains("/api/auth/") ||
                    exchange.getRequest().getURI().getPath().contains("/api/member/")) {
                headers.add("Cache-Control", "no-store, no-cache, must-revalidate, private");
                headers.add("Pragma", "no-cache");
            }
        }));
    }

    @Override
    public int getOrder() {
        // Run after all other filters to ensure headers are added last
        return Ordered.LOWEST_PRECEDENCE;
    }
}
