package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry.RouteInfo;
import com.gdn.project.waroenk.gateway.service.StaticRouteRegistry.ServiceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to resolve HTTP requests to gRPC service/method configurations.
 * 
 * Simplified version that only uses static routes from application.properties.
 * No dynamic registration, no database, no Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteResolver {

    private final StaticRouteRegistry routeRegistry;

    /**
     * Resolve the route for a given HTTP method and path.
     */
    public Optional<ResolvedRoute> resolve(String httpMethod, String path) {
        return routeRegistry.resolveRoute(httpMethod, path)
                .map(this::toResolvedRoute);
    }

    /**
     * Get all routes.
     */
    public List<ResolvedRoute> getAllRoutes() {
        return routeRegistry.getAllRoutes().stream()
                .map(this::toResolvedRoute)
                .collect(Collectors.toList());
    }

    /**
     * Get the service info for a service name.
     */
    public Optional<ServiceInfo> getServiceInfo(String serviceName) {
        return routeRegistry.getServiceInfo(serviceName);
    }

    /**
     * Extract path variables from a path based on the pattern.
     */
    public Map<String, String> extractPathVariables(String pattern, String path) {
        return routeRegistry.extractPathVariables(pattern, path);
    }

    private ResolvedRoute toResolvedRoute(RouteInfo info) {
        return new ResolvedRoute(
                info.serviceName(),
                info.grpcServiceName(),
                info.grpcMethodName(),
                null, // requestType - not needed with reflection
                null, // responseType - not needed with reflection
                info.publicEndpoint(),
                info.requiredRoles(),
                info.httpPath()
        );
    }

    /**
     * Resolved route information containing all details needed to make a gRPC call.
     */
    public record ResolvedRoute(
            String serviceName,
            String grpcServiceName,
            String grpcMethodName,
            String requestType,
            String responseType,
            boolean publicEndpoint,
            List<String> requiredRoles,
            String httpPathPattern  // The pattern used to match, e.g. /api/merchant/{id}
    ) {}
}
