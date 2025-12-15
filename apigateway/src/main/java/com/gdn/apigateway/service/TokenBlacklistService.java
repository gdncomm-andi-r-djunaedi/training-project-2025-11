package com.gdn.apigateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

  private static final String BLACKLIST_PREFIX = "blacklist:";

  private final ReactiveRedisTemplate<String, String> redisTemplate;

  /**
   * Add token to blacklist with TTL matching token's remaining lifetime
   */
  public Mono<Boolean> blacklistToken(String token, Duration ttl) {
    String key = BLACKLIST_PREFIX + token;
    log.info("Blacklisting token with TTL: {} seconds", ttl.getSeconds());
    return redisTemplate.opsForValue()
        .set(key, "blacklisted", ttl)
        .doOnSuccess(result -> log.info("Token blacklisted successfully"))
        .doOnError(error -> log.error("Failed to blacklist token: {}", error.getMessage()));
  }

  /**
   * Check if token is blacklisted
   */
  public Mono<Boolean> isBlacklisted(String token) {
    String key = BLACKLIST_PREFIX + token;
    return redisTemplate.hasKey(key)
        .doOnNext(isBlacklisted -> {
          if (isBlacklisted) {
            log.debug("Token is blacklisted");
          }
        });
  }
}

