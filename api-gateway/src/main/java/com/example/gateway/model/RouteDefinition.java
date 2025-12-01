package com.example.gateway.model;

public record RouteDefinition(
    String id,
    String pathPrefix,
    String targetBaseUrl,
    String targetPathPrefix,
    boolean requiresAuth
) {}