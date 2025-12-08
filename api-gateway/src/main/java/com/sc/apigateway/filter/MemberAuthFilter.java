package com.sc.apigateway.filter;

import com.sc.apigateway.dto.ValidateTokenResponseWrapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class MemberAuthFilter extends AbstractGatewayFilterFactory<MemberAuthFilter.Config> {

    private final WebClient webClient;

    public MemberAuthFilter(WebClient.Builder builder) {
        super(Config.class);
        this.webClient = builder.baseUrl("http://localhost:8091").build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing Authorization Header");
            }

            return webClient.get()
                    .uri("/members/validate")
                    .header("Authorization", authHeader)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
                            return Mono.just(
                                    ValidateTokenResponseWrapper.failure("Invalid or expired token")
                            );
                        }
                        if (clientResponse.statusCode().isError()) {
                            return Mono.just(
                                    ValidateTokenResponseWrapper.failure("Member validation service error")
                            );
                        }
                        return clientResponse.bodyToMono(ValidateTokenResponseWrapper.class);
                    })
                    .flatMap(res -> {
                        if (!res.isSuccess()) {
                            return onError(exchange, res.getErrorMessage());
                        }
                        String memberId = res.getData().getMemberId();
                        URI newUri = UriComponentsBuilder
                                .fromUri(exchange.getRequest().getURI())
                                .replaceQueryParam("memberId", memberId)
                                .build()
                                .toUri();
                        ServerHttpRequest mutatedRequest =
                                exchange.getRequest().mutate().uri(newUri).build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });

        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        String jsonError = String.format(
                "{\"timestamp\": \"%s\", \"success\": false, \"errorCode\": \"UNAUTHORIZED\", \"errorMessage\": \"%s\", \"data\": null}",
                Instant.now(), errorMsg
        );
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        DataBuffer buffer = response.bufferFactory().wrap(jsonError.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
