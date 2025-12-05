package com.gdn.training.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long durationInMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", durationInMillis, TimeUnit.MILLISECONDS);
    }
}
