package com.gdn.project.waroenk.member.config;

import com.gdn.project.waroenk.member.utility.CacheUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Test configuration that provides mock implementations of dependencies
 * that require external services (like Redis).
 */
@TestConfiguration
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

