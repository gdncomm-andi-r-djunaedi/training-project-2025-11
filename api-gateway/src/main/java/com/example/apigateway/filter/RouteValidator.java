package com.example.apigateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

        public static final List<String> openApiEndpoints = List.of(
                        "/auth/register",
                        "/auth/login",
                        "/auth/logout",
                        "/product",
                        "/product/**",
                        "/api/products",
                        "/api/products/**");

        public Predicate<ServerHttpRequest> isSecured = request -> {
                String path = request.getURI().getPath();
                return openApiEndpoints.stream()
                                .noneMatch(path::startsWith);
        };

}
