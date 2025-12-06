package com.marketplace.cart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Cart Service
 * Configures JWT Bearer token authentication in Swagger UI
 */
@Configuration
public class SwaggerConfig {

    @Value("${springdoc.servers[0].url:http://localhost:8083}")
    private String serverUrl;

    @Value("${springdoc.servers[0].description:Cart Service}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl).description(serverDescription)))
                .info(new Info()
                        .title("Cart Service API")
                        .version("1.0.0")
                        .description("API for managing shopping cart operations"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")));
    }
}
