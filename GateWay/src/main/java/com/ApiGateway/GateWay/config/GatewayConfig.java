package com.ApiGateway.GateWay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Configuration
public class GatewayConfig {
    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()

                // MEMBER SERVICE ROUTES
                .route(RequestPredicates.path("/member/**"),
                        req -> {
                            URI uri = URI.create("http://localhost:8093" + req.path());
                            return ServerResponse.temporaryRedirect(uri).build();
                        })

                // CART SERVICE ROUTES
                .route(RequestPredicates.path("/cart/**"),
                        req -> {
                            URI uri = URI.create("http://localhost:8092" + req.path());
                            return ServerResponse.temporaryRedirect(uri).build();
                        })

                .build();
    }
}
