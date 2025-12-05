package com.gdn.training.apigateway.filter;

import com.gdn.training.apigateway.service.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator routerValidator;
    private final JwtService jwtService;

    public AuthenticationFilter(RouterValidator routerValidator, JwtService jwtService) {
        super(Config.class);
        this.routerValidator = routerValidator;
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (routerValidator.isSecured.test(exchange.getRequest())) {
                // Missing Authorization header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    var response = exchange.getResponse();
                    response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                    response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    var buffer = response.bufferFactory().wrap("{\"error\":\"Missing Authorization Header\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    return response.writeWith(reactor.core.publisher.Mono.just(buffer));
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }
                try {
                    jwtService.validateToken(authHeader);
                } catch (Exception e) {
                    var response = exchange.getResponse();
                    response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                    response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    var buffer = response.bufferFactory().wrap(("{\"error\":\"" + e.getMessage() + "\"}").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    return response.writeWith(reactor.core.publisher.Mono.just(buffer));
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
