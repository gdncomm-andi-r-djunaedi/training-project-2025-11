package com.project.api_gateway;

import main.java.com.project.api_gateway.config.GatewayRoutesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.project.api_gateway", "main.java.com.project.api_gateway"})
@ConfigurationPropertiesScan(basePackages = {"com.project.api_gateway.config", "main.java.com.project.api_gateway.config"})
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder, GatewayRoutesProperties props) {
		RouteLocatorBuilder.Builder r = builder.routes();
		for (GatewayRoutesProperties.Route route : props.getRoutes()) {
			if (!route.isEnabled()) continue;
			r = r.route(route.getId(), spec -> spec.path(route.getPath())
					.filters(f -> {
						Integer strip = route.getStripPrefix();
						if (strip != null) {
							f.stripPrefix(strip);
						}
						return f;
					})
					.uri(route.getUri()));
		}
		return r.build();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of("*"));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}
}
