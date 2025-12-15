package com.project.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application
 * 
 * This gateway service provides:
 * - JWT token validation
 * - CORS configuration
 * - Route management to backend services
 * - Rate limiting using Redis
 * - Global exception handling
 */
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
