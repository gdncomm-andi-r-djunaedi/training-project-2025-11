package com.marketplace.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Swagger/OpenAPI documentation.
 * Can be customized per service via application.properties.
 */
@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    /**
     * API title displayed in Swagger UI
     */
    private String title = "API";

    /**
     * API description
     */
    private String description = "API Documentation";

    /**
     * API version
     */
    private String version = "1.0.0";

    /**
     * Server URL for API requests
     */
    private String serverUrl = "http://localhost:8080";

    /**
     * Server description
     */
    private String serverDescription = "Local Server";

    /**
     * Whether to enable JWT Bearer authentication in Swagger UI
     */
    private boolean enableBearerAuth = true;
}

