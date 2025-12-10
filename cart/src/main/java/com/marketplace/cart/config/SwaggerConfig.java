package com.marketplace.cart.config;

import com.marketplace.common.config.BaseSwaggerConfig;
import com.marketplace.common.config.SwaggerProperties;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration for Cart Service.
 * Uses common BaseSwaggerConfig with service-specific properties.
 */
@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(SwaggerProperties properties) {
        return BaseSwaggerConfig.createOpenAPI(properties);
    }
}
