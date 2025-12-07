package com.marketplace.gateway.service;

import com.marketplace.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * Blacklists a token until its expiration.
     */
    public Mono<Boolean> blacklistToken(String token) {
        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();

            if (ttlMillis <= 0) {
                log.debug("Token already expired, no need to blacklist");
                return Mono.just(true);
            }

            String key = BLACKLIST_PREFIX + token;
            return redisTemplate.opsForValue()
                    .set(key, "blacklisted", Duration.ofMillis(ttlMillis))
                    .doOnSuccess(success -> log.debug("Token blacklisted successfully: {}", success))
                    .doOnError(e -> log.error("Error blacklisting token: {}", e.getMessage()));
        } catch (Exception e) {
            log.error("Error calculating token expiration for blacklist: {}", e.getMessage());
            return Mono.just(false);
        }
    }

    /**
     * Checks if a token is blacklisted.
     */
    public Mono<Boolean> isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}

