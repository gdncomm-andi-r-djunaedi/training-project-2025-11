package com.example.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Route Locator that reads route configurations from properties
 * and conditionally routes through internal gateway based on 'authenticated' flag.
 * 
 * Routes are configured using standard Spring Cloud Gateway format:
 * spring.cloud.gateway.routes[0].id=route-id
 * spring.cloud.gateway.routes[0].uri=http://service-url
 * spring.cloud.gateway.routes[0].predicates[0]=Path=/api/path/**
 * spring.cloud.gateway.routes[0].authenticated=true/false
 */
@Slf4j
@Configuration
public class DynamicRouteLocator {

    @Value("${gateway.internal-gateway.url:http://localhost:8088}")
    private String internalGatewayUrl;

    private final Environment environment;

    public DynamicRouteLocator(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring routes dynamically from properties");
        log.info("Internal Gateway URL: {}", internalGatewayUrl);

        RouteLocatorBuilder.Builder routes = builder.routes();
        List<RouteConfig> routeConfigs = loadRouteConfigurations();

        for (RouteConfig config : routeConfigs) {
            String finalUri = config.isAuthenticated() ? internalGatewayUrl : config.getUri();
            String logMessage = config.isAuthenticated() 
                ? "Route '{}' configured to use internal gateway (authenticated)" 
                : "Route '{}' configured to route directly to {} (not authenticated)";
            
            log.info(logMessage, config.getId(), config.isAuthenticated() ? "internal gateway" : config.getUri());

            routes.route(config.getId(), r -> {
                var routeBuilder = r.path(config.getPath());
                
                // Apply any additional predicates if needed in the future
                if (config.getPredicates() != null && !config.getPredicates().isEmpty()) {
                    // Can add more predicate logic here if needed
                }
                
                return routeBuilder.uri(finalUri);
            });
        }

        return routes.build();
    }

    /**
     * Load route configurations from Environment properties
     */
    private List<RouteConfig> loadRouteConfigurations() {
        List<RouteConfig> configs = new ArrayList<>();
        int index = 0;

        while (true) {
            String idKey = String.format("spring.cloud.gateway.routes[%d].id", index);
            String id = environment.getProperty(idKey);
            
            if (id == null) {
                break; // No more routes
            }

            String uriKey = String.format("spring.cloud.gateway.routes[%d].uri", index);
            String uri = environment.getProperty(uriKey);
            
            String authenticatedKey = String.format("spring.cloud.gateway.routes[%d].authenticated", index);
            boolean authenticated = environment.getProperty(authenticatedKey, Boolean.class, false);
            
            // Get path predicate (assuming first predicate is Path)
            String pathKey = String.format("spring.cloud.gateway.routes[%d].predicates[0]", index);
            String pathPredicate = environment.getProperty(pathKey);
            String path = extractPathFromPredicate(pathPredicate);

            if (uri != null && path != null) {
                configs.add(new RouteConfig(id, uri, path, authenticated));
                log.debug("Loaded route config: id={}, uri={}, path={}, authenticated={}", 
                    id, uri, path, authenticated);
            } else {
                log.warn("Incomplete route configuration at index {}: id={}, uri={}, path={}", 
                    index, id, uri, path);
            }

            index++;
        }

        log.info("Loaded {} route configurations", configs.size());
        return configs;
    }

    /**
     * Extract path pattern from predicate string (e.g., "Path=/api/cart/**" -> "/api/cart/**")
     */
    private String extractPathFromPredicate(String predicate) {
        if (predicate == null) {
            return null;
        }
        
        // Handle format: "Path=/api/cart/**" or "Path=/api/cart/**"
        if (predicate.startsWith("Path=")) {
            return predicate.substring(5); // Remove "Path=" prefix
        }
        
        return predicate;
    }

    /**
     * Internal class to hold route configuration
     */
    private static class RouteConfig {
        private final String id;
        private final String uri;
        private final String path;
        private final boolean authenticated;
        private final List<String> predicates;

        public RouteConfig(String id, String uri, String path, boolean authenticated) {
            this.id = id;
            this.uri = uri;
            this.path = path;
            this.authenticated = authenticated;
            this.predicates = new ArrayList<>();
            if (path != null) {
                this.predicates.add("Path=" + path);
            }
        }

        public String getId() {
            return id;
        }

        public String getUri() {
            return uri;
        }

        public String getPath() {
            return path;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public List<String> getPredicates() {
            return predicates;
        }
    }
}

