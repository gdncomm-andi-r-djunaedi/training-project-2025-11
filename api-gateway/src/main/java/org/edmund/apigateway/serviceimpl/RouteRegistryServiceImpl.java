package org.edmund.apigateway.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.edmund.apigateway.dto.ResolvedRoute;
import org.edmund.apigateway.dto.Router;
import org.edmund.apigateway.properties.GatewayProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteRegistryServiceImpl implements org.edmund.apigateway.services.RouteRegistryService {

  private final GatewayProperties properties;

  @Override
  public ResolvedRoute resolve(String requestPath) {
    Router def = properties.getRoutesList().stream()
        .filter(r -> requestPath.startsWith(r.getPathPrefix()))
        .findFirst()
        .orElse(null);

    if (def == null) {
      return null;
    }

    String remaining = requestPath.substring(def.getPathPrefix().length());
    if (remaining.equals("/")) {
      remaining = "";
    }

    return new ResolvedRoute(def, remaining);
  }
}
