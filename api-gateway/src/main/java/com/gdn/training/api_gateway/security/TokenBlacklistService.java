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

    public void blacklist(String jti, Duration ttl) {
        if (!StringUtils.hasText(jti) || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisTemplate.opsForValue().set(buildKey(jti), "1", ttl);
    }

    public boolean isBlacklisted(String jti) {
        return StringUtils.hasText(jti)
                && Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(jti)));
    }

    private String buildKey(String jti) {
        return KEY_PREFIX + jti;
    }
}

