package com.dev.onlineMarketplace.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

@Configuration
public class CorsConfig {

    @Bean
    public GlobalFilter corsResponseFilter() {
        return new CorsResponseFilter();
    }

    private static class CorsResponseFilter implements GlobalFilter, Ordered {
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Handle OPTIONS preflight request immediately
            if (request.getMethod() == HttpMethod.OPTIONS) {
                HttpHeaders headers = response.getHeaders();
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
                response.setStatusCode(HttpStatus.OK);
                return response.setComplete();
            }

            // For all other requests, wrap response to add CORS headers
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends org.springframework.core.io.buffer.DataBuffer> body) {
                    HttpHeaders headers = getHeaders();
                    if (!headers.containsKey(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
                        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }

        @Override
        public int getOrder() {
            // Run before NettyWriteResponseFilter to ensure headers are set
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }
    }
}
