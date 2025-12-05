package org.edmund.apigateway.services;
import org.edmund.apigateway.dto.ResolvedRoute;

public interface RouteRegistryService {
  ResolvedRoute resolve(String requestPath);
}