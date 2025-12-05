package com.dev.onlineMarketplace.gateway.filter;

import com.dev.onlineMarketplace.gateway.config.SecurityConfig;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Validates routes to determine if they require authentication
 * Uses configuration from SecurityConfig for flexibility
 */
@Component
public class RouteValidator {

    private final SecurityConfig securityConfig;

    public RouteValidator(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Predicate to check if a request requires authentication
     * Returns true if the endpoint is secured (requires auth)
     * Returns false if the endpoint is public (no auth needed)
     */
    public Predicate<ServerHttpRequest> isSecured = request -> isEndpointSecured(request);

    /**
     * Checks if an endpoint is secured (requires authentication)
     */
    private boolean isEndpointSecured(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        
        // Check if the path matches any open endpoint pattern
        boolean isOpen = securityConfig.getOpenEndpoints()
                .stream()
                .anyMatch(openPath -> matchesPattern(path, openPath));
        
        // If it's an open endpoint, it's not secured
        return !isOpen;
    }

    /**
     * Matches a path against a pattern
     * Supports wildcards like /api/v1/products/**
     */
    private boolean matchesPattern(String path, String pattern) {
        // Exact match
        if (path.equals(pattern)) {
            return true;
        }
        
        // Pattern with /** wildcard (matches any subpath)
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(basePattern);
        }
        
        // Pattern with * wildcard (matches within path segment)
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return path.matches(regex);
        }
        
        // No wildcard - check if path starts with pattern (for backward compatibility)
        // This allows patterns like "/swagger-ui" to match "/swagger-ui/index.html"
        if (path.startsWith(pattern)) {
            return true;
        }
        
        return false;
    }
}
