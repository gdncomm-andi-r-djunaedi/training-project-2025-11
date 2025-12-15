package com.training.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiGatewayConfig {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
