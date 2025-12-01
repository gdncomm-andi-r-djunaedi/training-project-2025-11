package com.example.gateway.routing;

import com.example.gateway.properties.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouteRegistry {

  private final GatewayProperties properties;

  public RouteDefinition findRoute(String requestPath) {
    return properties.getRoutes().stream()
        .filter(r -> requestPath.startsWith(r.pathPrefix()))
        .findFirst()
        .orElse(null);
  }
}