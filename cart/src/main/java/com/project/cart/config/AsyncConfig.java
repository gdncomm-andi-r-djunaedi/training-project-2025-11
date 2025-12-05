package com.project.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Async and scheduling configuration
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
