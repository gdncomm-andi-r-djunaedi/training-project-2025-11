package com.example.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String method = request.getMethod().toString();
        String path = request.getURI().getPath();
        String queryParams = Optional.ofNullable(request.getURI().getQuery())
                .orElse("");
        String remoteAddress = Optional.ofNullable(request.getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");

        log.debug("=== API Gateway Request ===");
        log.debug("Timestamp: {}", LocalDateTime.now());
        log.debug("Method: {}", method);
        log.debug("Path: {}", path);
        if (!queryParams.isEmpty()) {
            log.debug("Query Params: {}", queryParams);
        }
        log.debug("Remote Address: {}", remoteAddress);
        log.debug("Headers: {}", request.getHeaders());
        log.debug("==========================");

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // High priority to log before other filters
    }
}
