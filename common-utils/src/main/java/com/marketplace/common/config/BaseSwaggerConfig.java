package com.marketplace.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * Base configuration for Swagger/OpenAPI documentation.
 * Provides common OpenAPI setup that can be reused across services.
 */
public class BaseSwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Creates an OpenAPI configuration with the provided properties.
     *
     * @param properties Swagger configuration properties
     * @return Configured OpenAPI instance
     */
    public static OpenAPI createOpenAPI(SwaggerProperties properties) {
        OpenAPI openAPI = new OpenAPI()
                .servers(List.of(new Server()
                        .url(properties.getServerUrl())
                        .description(properties.getServerDescription())))
                .info(new Info()
                        .title(properties.getTitle())
                        .version(properties.getVersion())
                        .description(properties.getDescription()));

        if (properties.isEnableBearerAuth()) {
            openAPI.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                    .components(new Components()
                            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                    new SecurityScheme()
                                            .name(SECURITY_SCHEME_NAME)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description("Enter JWT Bearer token")));
        }

        return openAPI;
    }
}

