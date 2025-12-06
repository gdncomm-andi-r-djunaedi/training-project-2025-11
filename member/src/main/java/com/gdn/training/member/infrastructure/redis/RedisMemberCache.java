package com.gdn.training.member.infrastructure.redis;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import lombok.extern.slf4j.Slf4j;

/**
 * helper for redis operations
 */
@Slf4j
public class RedisMemberCache {
    private final ValueOperations<String, Object> valueOps;
    private final Duration cacheDuration = Duration.ofMinutes(10);

    public RedisMemberCache(RedisTemplate<String, Object> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    public void putMinimalProfile(UUID id, String minimalProfilePayload) {
        try {
            valueOps.set("member:profile:" + id.toString(), minimalProfilePayload, cacheDuration);
        } catch (Exception e) {
            log.error("RedisMemberCache put minimal profile to cache", e);
        }
    }

    public void putEmailExists(String email) {
        try {
            valueOps.set("member:email-exists:" + email.toLowerCase(), "1", Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("RedisMemberCache put email exists to cache", e);
        }
    }

    public boolean emailExistsCached(String email) {
        try {
            return valueOps.get("member:email-exists:" + email.toLowerCase()) != null;
        } catch (Exception e) {
            log.error("RedisMemberCache email exists cached", e);
            return false;
        }
    }
}
