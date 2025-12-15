package com.blibli.api_gateway.routes;

import com.blibli.api_gateway.config.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JwtRoutes {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder,JwtAuthFilter jwtAuthFilter){
        return routeLocatorBuilder.routes()
                .route(
                        r-> r
                                .path("/auth/**")
                                .filters(f-> f.filter(jwtAuthFilter).rewritePath("/auth/(?<segment>.*)","/api/auth/${segment}"))
                                .uri("http://localhost:8081"))
                .route(
                        r-> r
                                .path("/cart/**")
                                .filters(f-> f.filter(jwtAuthFilter).rewritePath("/cart/(?<segment>.*)","/api/cart/${segment}"))
                                .uri("http://localhost:8083")
                )
                .route(
                        r-> r
                                .path("/product/**")
                                .filters(f->f.rewritePath("/product/(?<segment>.*)","/api/product/${segment}"))
                                .uri("http://localhost:8082")
                )
                .build();

    }
}
