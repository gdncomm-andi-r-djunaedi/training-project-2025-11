package com.example.gateway.service;

import com.example.gateway.model.ResolvedRoute;

public interface RouteRegistryService {

  /**
   * Resolve the request path into a concrete route and the remaining sub-path.
   *
   * @param requestPath e.g. "/api/members/register"
   * @return ResolvedRoute or null if no route matches
   */
  ResolvedRoute resolve(String requestPath);
}