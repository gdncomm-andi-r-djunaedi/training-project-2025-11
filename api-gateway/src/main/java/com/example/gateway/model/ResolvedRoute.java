package com.example.gateway.model;

/**
 * Value object that represents a resolved route for a given request path.
 */
public record ResolvedRoute(
    RouteDefinition definition,
    String remainingPath // e.g. "/register" when request was "/api/members/register"
) {}
