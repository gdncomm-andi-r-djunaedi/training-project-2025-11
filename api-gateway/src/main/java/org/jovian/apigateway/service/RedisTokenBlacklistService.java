package org.jovian.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "BLACKLIST:";

    /**
     * Add a JWT token to the blacklist
     * @param token JWT string
     * @param expirationSeconds token lifetime in seconds (from JWT expiration)
     */
    public void blacklistToken(String token, long expirationSeconds) {
        redisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + token, "1", expirationSeconds, TimeUnit.SECONDS);
    }

    /**
     * Check if a JWT token is blacklisted
     * @param token JWT string
     * @return true if blacklisted
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
