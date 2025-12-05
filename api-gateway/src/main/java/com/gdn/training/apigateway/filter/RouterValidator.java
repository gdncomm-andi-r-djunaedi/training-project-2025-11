package com.gdn.training.apigateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

        public static final List<String> openApiEndpoints = List.of(
                        "/auth/register",
                        "/auth/login",
                        "/auth/generate-token",
                        "/eureka",
                        "/member/api/members/register",
                        "/member/api/members/login",
                        "/member/api/members/username",
                        "/product/api/products/name",
                        "/product/api/products/all");

        public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
                        .stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
