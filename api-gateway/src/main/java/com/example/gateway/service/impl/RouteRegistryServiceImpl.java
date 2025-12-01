package com.example.gateway.service.impl;

import com.example.gateway.properties.GatewayProperties;
import com.example.gateway.model.ResolvedRoute;
import com.example.gateway.model.RouteDefinition;
import com.example.gateway.service.RouteRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteRegistryServiceImpl implements RouteRegistryService {

  private final GatewayProperties properties;

  @Override
  public ResolvedRoute resolve(String requestPath) {
    RouteDefinition def = properties.getRoutes().stream()
        .filter(r -> requestPath.startsWith(r.pathPrefix()))
        .findFirst()
        .orElse(null);

    if (def == null) {
      return null;
    }

    String remaining = requestPath.substring(def.pathPrefix().length());
    // normalize: "/api/cart" -> remaining "" (so downstream URL is /api/cart, no trailing /)
    if (remaining.equals("/")) {
      remaining = "";
    }

    return new ResolvedRoute(def, remaining);
  }
}
