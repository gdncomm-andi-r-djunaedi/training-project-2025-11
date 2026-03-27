package com.marketplace.gateway.config;

import com.marketplace.gateway.filter.JwtAuthenticationFilter;
import com.marketplace.gateway.filter.JwtLogoutFilter;
import com.marketplace.gateway.filter.JwtTokenCreationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RouteProperties.class)
public class GatewayConfig {

    private final JwtTokenCreationFilter jwtTokenCreationFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutFilter jwtLogoutFilter;
    private final RouteProperties routeProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        var jwtAuthFilter = jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config());
        var jwtLogoutFilterInstance = jwtLogoutFilter.apply(new JwtLogoutFilter.Config());

        var routesBuilder = builder.routes();
        routeProperties.getRoutes().forEach(route -> {
            log.info("Configuring route: id={}, path={}, uri={}, filters={}",
                    route.getId(), route.getPath(), route.getUri(), route.getFilters());

            routesBuilder.route(route.getId(), r -> {
                var pathSpec = r.path(route.getPath());
                if (route.getFilters() != null && !route.getFilters().isBlank()) {
                    return pathSpec.filters(f -> {
                        Arrays.stream(route.getFilters().split(","))
                                .map(String::trim)
                                .forEach(filterName -> {
                                    switch (filterName) {
                                        case "jwt-auth" -> f.filter(jwtAuthFilter);
                                        case "jwt-token-creation" -> f.modifyResponseBody(String.class, String.class,
                                                (exchange, originalBody) -> jwtTokenCreationFilter.modifyResponse(exchange, originalBody));
                                        case "jwt-logout" -> f.filter(jwtLogoutFilterInstance);
                                        default -> {
                                            if (!filterName.isBlank()) {
                                                log.warn("Unknown filter type: {}", filterName);
                                            }
                                        }
                                    }
                                });
                        return f;
                    }).uri(route.getUri());
                }
                return pathSpec.uri(route.getUri());
            });
        });

        return routesBuilder.build();
    }
}

