package com.gdn.project.waroenk.gateway.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Test configuration providing mock beans for external dependencies.
 */
@TestConfiguration
public class TestConfiguration {

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    return Mockito.mock(RedisConnectionFactory.class);
  }

  @Bean
  @Primary
  public RedisTemplate<String, String> redisTemplate() {
    @SuppressWarnings("unchecked")
    RedisTemplate<String, String> mockTemplate = Mockito.mock(RedisTemplate.class);
    return mockTemplate;
  }

  @Bean
  @Primary
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}

