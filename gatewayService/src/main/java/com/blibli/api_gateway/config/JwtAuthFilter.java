package com.blibli.api_gateway.config;

import com.blibli.api_gateway.utils.BlockListToken;
import com.blibli.api_gateway.utils.JwtUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
//import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;


@Component
public class JwtAuthFilter implements GatewayFilter {
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    BlockListToken blockListToken;
    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if(exchange.getRequest().getPath().toString().contains("/auth")){
            if(exchange.getRequest().getPath().toString().contains("/logout")){
                blockListToken.addBlockListToken(exchange.getRequest().getQueryParams().getFirst("token"));
            }
            return chain.filter(exchange);
        }
        else {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String username = jwtUtils.extractUsername(token);

            if (jwtUtils.validateToken(username, token) == false) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            UriBuilder uriBuilder = UriComponentsBuilder.fromUri(exchange.getRequest().getURI());

            if (exchange.getRequest().getPath().toString().contains("/cart"))
                uriBuilder.queryParam("customerEmail", username);


            ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .uri(uriBuilder.build(true).toURL().toURI())
                    .build();


            return chain.filter(exchange.mutate().request(mutateRequest).build());
        }

    }
}
