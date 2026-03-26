package com.ApiGateway.GateWay.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ActiveTokenStore {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX = "TOKEN_";

    public void saveToken(String username, String token, long expirySeconds) {
        redisTemplate.opsForValue().set(PREFIX + username, token, expirySeconds, TimeUnit.SECONDS);
    }

    public String getToken(String username) {
        return redisTemplate.opsForValue().get(PREFIX + username);
    }

    public void removeToken(String username) {
        redisTemplate.delete(PREFIX + username);
    }
}
