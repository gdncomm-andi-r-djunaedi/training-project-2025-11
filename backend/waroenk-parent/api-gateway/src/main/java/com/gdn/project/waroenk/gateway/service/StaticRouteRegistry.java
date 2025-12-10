package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.config.GatewayProperties;
import com.gdn.project.waroenk.gateway.config.GatewayProperties.MethodMapping;
import com.gdn.project.waroenk.gateway.config.GatewayProperties.RouteConfig;
import com.gdn.project.waroenk.gateway.config.GatewayProperties.ServiceConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.*;

/**
 * Lightweight, static route registry that loads all routes from application.properties.
 * 
 * Memory optimized:
 * - No database (PostgreSQL) required
 * - No Redis caching required
 * - All routes loaded once at startup
 * - Simple in-memory HashMap for O(1) lookups
 * - Uses AntPathMatcher for path variable matching
 * 
 * This replaces the DynamicRoutingRegistry for environments where:
 * - Routes are static and known at deployment time
 * - Memory footprint needs to be minimized
 * - External dependencies (DB, Redis) should be avoided
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaticRouteRegistry {

    private final GatewayProperties gatewayProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Route cache: "METHOD:PATH" -> RouteInfo
    private final Map<String, RouteInfo> routeCache = new HashMap<>();
    
    // Pattern routes: for paths with variables like /api/user/{id}
    private final List<PatternRoute> patternRoutes = new ArrayList<>();

    @PostConstruct
    public void initialize() {
        log.info("Initializing static route registry from properties...");
        loadRoutesFromProperties();
        log.info("Static route registry initialized with {} routes for {} services",
                routeCache.size() + patternRoutes.size(), 
                gatewayProperties.getServices().size());
    }

    /**
     * Load all routes from GatewayProperties into memory cache.
     */
    private void loadRoutesFromProperties() {
        for (RouteConfig route : gatewayProperties.getRoutes()) {
            String serviceName = route.getService();
            String grpcServiceName = route.getGrpcService();
            boolean routePublic = route.isPublicRoute();

            for (MethodMapping method : route.getMethods()) {
                RouteInfo routeInfo = new RouteInfo(
                        serviceName,
                        grpcServiceName,
                        method.getGrpcMethod(),
                        method.getHttpMethod(),
                        method.getHttpPath(),
                        method.isPublicEndpoint() || routePublic,
                        method.getRequiredRoles() != null ? method.getRequiredRoles() : List.of()
                );

                String key = buildKey(method.getHttpMethod(), method.getHttpPath());
                
                // Check if path contains variables like {id}
                if (method.getHttpPath().contains("{")) {
                    patternRoutes.add(new PatternRoute(
                            method.getHttpMethod().toUpperCase(),
                            method.getHttpPath(),
                            routeInfo
                    ));
                    log.debug("Registered pattern route: {} {} -> {}.{}", 
                            method.getHttpMethod(), method.getHttpPath(),
                            grpcServiceName, method.getGrpcMethod());
                } else {
                    routeCache.put(key, routeInfo);
                    log.debug("Registered exact route: {} {} -> {}.{}", 
                            method.getHttpMethod(), method.getHttpPath(),
                            grpcServiceName, method.getGrpcMethod());
                }
            }
        }
    }

    /**
     * Resolve a route by HTTP method and path.
     * First tries exact match, then pattern match.
     */
    public Optional<RouteInfo> resolveRoute(String httpMethod, String path) {
        String key = buildKey(httpMethod, path);

        // 1. Try exact match first (O(1) lookup)
        RouteInfo route = routeCache.get(key);
        if (route != null) {
            return Optional.of(route);
        }

        // 2. Try pattern matching for paths with variables
        String upperMethod = httpMethod.toUpperCase();
        for (PatternRoute pattern : patternRoutes) {
            if (pattern.httpMethod.equals(upperMethod) && 
                pathMatcher.match(pattern.pathPattern, path)) {
                return Optional.of(pattern.routeInfo);
            }
        }

        return Optional.empty();
    }

    /**
     * Get service configuration by name.
     */
    public Optional<ServiceInfo> getServiceInfo(String serviceName) {
        ServiceConfig config = gatewayProperties.getServices().get(serviceName);
        if (config != null) {
            return Optional.of(new ServiceInfo(
                    serviceName,
                    config.getHost(),
                    config.getPort(),
                    config.isUseTls()
            ));
        }
        return Optional.empty();
    }

    /**
     * Extract path variables from a path based on the pattern.
     */
    public Map<String, String> extractPathVariables(String pattern, String path) {
        return pathMatcher.extractUriTemplateVariables(pattern, path);
    }

    /**
     * Get all registered routes (for /routes endpoint).
     */
    public Collection<RouteInfo> getAllRoutes() {
        List<RouteInfo> allRoutes = new ArrayList<>(routeCache.values());
        for (PatternRoute pattern : patternRoutes) {
            allRoutes.add(pattern.routeInfo);
        }
        return allRoutes;
    }

    /**
     * Get all registered services (for /services endpoint).
     */
    public Collection<ServiceInfo> getAllServices() {
        List<ServiceInfo> services = new ArrayList<>();
        gatewayProperties.getServices().forEach((name, config) -> {
            services.add(new ServiceInfo(name, config.getHost(), config.getPort(), config.isUseTls()));
        });
        return services;
    }

    /**
     * Get total route count.
     */
    public int getRouteCount() {
        return routeCache.size() + patternRoutes.size();
    }

    /**
     * Get total service count.
     */
    public int getServiceCount() {
        return gatewayProperties.getServices().size();
    }

    private String buildKey(String method, String path) {
        return method.toUpperCase() + ":" + path;
    }

    // ==================== Record Classes ====================

    /**
     * Route information record.
     */
    public record RouteInfo(
            String serviceName,
            String grpcServiceName,
            String grpcMethodName,
            String httpMethod,
            String httpPath,
            boolean publicEndpoint,
            List<String> requiredRoles
    ) {}

    /**
     * Service connection information.
     */
    public record ServiceInfo(
            String name,
            String host,
            int port,
            boolean useTls
    ) {}

    /**
     * Pattern route for paths with variables.
     */
    private record PatternRoute(
            String httpMethod,
            String pathPattern,
            RouteInfo routeInfo
    ) {}
}


