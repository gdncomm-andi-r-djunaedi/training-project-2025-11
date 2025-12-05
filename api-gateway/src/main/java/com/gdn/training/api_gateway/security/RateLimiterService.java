package com.gdn.training.api_gateway.security;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gdn.training.api_gateway.config.RateLimiterProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private static final String KEY_PREFIX = "ratelimit:";
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final RateLimiterProperties properties;
    private final StringRedisTemplate redisTemplate;

    public RateLimitResult consume(String key) {
        long limit = Math.max(1, properties.getRequestsPerMinute());
        String redisKey = buildKey(StringUtils.hasText(key) ? key : "anonymous");

        Long currentCount = increment(redisKey);
        if (currentCount == null) {
            return RateLimitResult.allowed(limit);
        }

        long nanosToReset = remainingNanos(redisKey);
        if (currentCount <= limit) {
            long remaining = Math.max(0, limit - currentCount);
            return RateLimitResult.allowed(remaining, nanosToReset);
        }

        return RateLimitResult.blocked(nanosToReset);
    }

    public long getConfiguredLimit() {
        return Math.max(1, properties.getRequestsPerMinute());
    }

    private Long increment(String key) {
        try {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            Long current = ops.increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, WINDOW);
            }
            return current;
        } catch (DataAccessException ex) {
            log.warn("Unable to increment rate limit key {}: {}", key, ex.getMessage());
            return null;
        }
    }

    private long remainingNanos(String key) {
        try {
            Long expireMillis = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            if (expireMillis == null || expireMillis < 0) {
                return WINDOW.toNanos();
            }
            return TimeUnit.MILLISECONDS.toNanos(expireMillis);
        } catch (DataAccessException ex) {
            log.warn("Unable to fetch TTL for rate limit key {}: {}", key, ex.getMessage());
            return WINDOW.toNanos();
        }
    }

    private String buildKey(String identifier) {
        return KEY_PREFIX + identifier;
    }
}
