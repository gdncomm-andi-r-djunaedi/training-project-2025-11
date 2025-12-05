package com.gdn.gateway.config;

import com.gdn.gateway.properties.GatewayRoutesProperties;
import com.gdn.gateway.utils.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicRouteConfig {

    private final GatewayRoutesProperties routeProps;
    private final JwtAuthFilter jwtAuthFilter;

    public DynamicRouteConfig(GatewayRoutesProperties routeProps,
                              JwtAuthFilter jwtAuthFilter) {
        this.routeProps = routeProps;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        routeProps.getRoutes().forEach(r -> {

            if (r.getName().equalsIgnoreCase("cart")) {
                routes.route(r.getName(),
                        p -> p.path(r.getPath())
                                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                                .uri(r.getUri())
                );
            } else {
                routes.route(r.getName(),
                        p -> p.path(r.getPath())
                                .uri(r.getUri())
                );
            }
        });

        return routes.build();
    }
}
