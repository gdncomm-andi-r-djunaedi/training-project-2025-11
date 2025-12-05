package com.ecommerce.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /**
     * Add a token to the blacklist with TTL matching JWT expiration
     * 
     * @param token             The JWT token to blacklist
     * @param expirationSeconds Time until token expires (should match JWT
     *                          expiration)
     * @return Mono<Boolean> indicating success
     */
    public Mono<Boolean> blacklistToken(String token, long expirationSeconds) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.opsForValue()
                .set(key, "blacklisted", Duration.ofSeconds(expirationSeconds));
    }

    /**
     * Check if a token is blacklisted
     * 
     * @param token The JWT token to check
     * @return Mono<Boolean> true if blacklisted, false otherwise
     */
    public Mono<Boolean> isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}
