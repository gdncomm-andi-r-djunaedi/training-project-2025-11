package com.blibi.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main application class for the Cart Service.
 * 
 * This class serves as the entry point for the Spring Boot application.
 * It enables MongoDB repositories and Feign clients for inter-service communication.
 * 
 * @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
 * @EnableMongoRepositories: Enables MongoDB repository support for data access
 * @EnableFeignClients: Enables Feign client functionality for calling external services
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@SpringBootApplication
@EnableMongoRepositories
@EnableFeignClients
public class CartApplication {

	/**
	 * Main method to start the Spring Boot application.
	 * 
	 * This method initializes the Spring application context and starts the embedded server.
	 * The application will run on the port specified in application.properties (default: 8081).
	 * 
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(CartApplication.class, args);
	}

}
