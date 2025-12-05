package com.project.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration
 * 
 * CORS is configured via application.properties using Spring Cloud Gateway's
 * built-in CORS support:
 * - spring.cloud.gateway.globalcors.cors-configurations
 * 
 * This class is kept for potential future programmatic CORS configuration
 * if needed. Currently, CORS is fully configured in application.properties.
 */
@Configuration
public class CorsConfig {
    // CORS configuration is handled via application.properties
    // using spring.cloud.gateway.globalcors properties
}

