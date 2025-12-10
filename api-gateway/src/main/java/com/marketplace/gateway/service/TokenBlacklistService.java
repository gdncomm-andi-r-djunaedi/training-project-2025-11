package com.marketplace.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    public Mono<Boolean> isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return reactiveRedisTemplate.hasKey(key)
                .doOnError(error -> log.error("Error checking token blacklist: {}", error.getMessage()))
                .onErrorReturn(false);
    }
}
