package com.gdn.apigateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

  // In-memory blacklist for testing
  private final Map<String, String> blacklistedTokens = new ConcurrentHashMap<>();

  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public ReactiveRedisTemplate<String, String> reactiveRedisTemplate() {
    ReactiveRedisTemplate<String, String> template = mock(ReactiveRedisTemplate.class);
    ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);

    when(template.opsForValue()).thenReturn(valueOps);

    // Mock set operation (blacklist token)
    when(valueOps.set(anyString(), anyString(), any(Duration.class)))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          blacklistedTokens.put(key, "blacklisted");
          return Mono.just(true);
        });

    // Mock hasKey operation (check blacklist)
    when(template.hasKey(anyString()))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          return Mono.just(blacklistedTokens.containsKey(key));
        });

    return template;
  }

  @Bean
  @SuppressWarnings("unchecked")
  public ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
    // Mock for Spring Cloud Gateway rate limiter (if needed)
    return mock(ReactiveStringRedisTemplate.class);
  }

  /**
   * Helper method to manually blacklist a token for testing
   */
  public void blacklistToken(String token) {
    blacklistedTokens.put("blacklist:" + token, "blacklisted");
  }

  /**
   * Clear all blacklisted tokens
   */
  public void clearBlacklist() {
    blacklistedTokens.clear();
  }
}

