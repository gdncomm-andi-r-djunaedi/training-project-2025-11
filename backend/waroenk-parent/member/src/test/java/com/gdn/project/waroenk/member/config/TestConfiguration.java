package com.gdn.project.waroenk.member.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Test configuration that provides mock implementations of dependencies
 * that require external services (like Redis).
 */
@SpringBootTest
public class TestConfiguration {

  /**
   * Mock RedisTemplate for testing.
   */
  @SuppressWarnings("unchecked")
  @Bean
  @Primary
  public <T> RedisTemplate<String, T> testRedisTemplate() {
    return Mockito.mock(RedisTemplate.class);
  }
}

