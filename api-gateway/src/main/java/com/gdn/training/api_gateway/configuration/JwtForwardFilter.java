package com.gdn.training.api_gateway.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Configuration
public class JwtForwardFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    // Only add header if JWT exists
                    if (auth != null
                            && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                        exchange.getRequest().mutate()
                                .header("Authorization", "Bearer " + jwt.getTokenValue())
                                .build();
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));

    }
}
