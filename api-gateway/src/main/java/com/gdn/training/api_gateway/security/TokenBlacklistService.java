package com.gdn.training.api_gateway.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jwtId, Duration ttl) {
        if (!StringUtils.hasText(jwtId) || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(buildKey(jwtId), "1", ttl);
    }

    public boolean isBlacklisted(String jwtId) {
        return StringUtils.hasText(jwtId)
                && Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(jwtId)));
    }

    private String buildKey(String jwtId) {
        return KEY_PREFIX + jwtId;
    }
}

