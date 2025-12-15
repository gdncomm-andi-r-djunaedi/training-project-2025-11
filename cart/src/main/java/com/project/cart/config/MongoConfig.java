package com.project.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.project.cart.repository")
@EnableMongoAuditing
public class MongoConfig {
}


