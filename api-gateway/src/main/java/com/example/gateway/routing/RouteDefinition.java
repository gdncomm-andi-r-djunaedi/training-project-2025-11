package com.example.gateway.routing;

public record RouteDefinition(
    String id,
    String pathPrefix,
    String targetBaseUrl,
    String targetPathPrefix,
    boolean requiresAuth
) {}