package com.dev.onlineMarketplace.gateway.service;

import com.dev.onlineMarketplace.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

/**
 * Service to manage token blacklisting using Redis
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public TokenBlacklistService(ReactiveStringRedisTemplate redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Add a token to the blacklist
     * The token will be stored in Redis with TTL equal to its remaining validity period
     *
     * @param token JWT token to blacklist
     * @return Mono<Boolean> true if successfully added
     */
    public Mono<Boolean> blacklistToken(String token) {
        try {
            // Extract expiration date from token
            Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
            Date now = new Date();
            
            // Calculate TTL (time until token expires)
            long ttlMillis = expirationDate.getTime() - now.getTime();
            
            if (ttlMillis <= 0) {
                logger.warn("Token already expired, no need to blacklist");
                return Mono.just(true);
            }

            String key = BLACKLIST_PREFIX + token;
            Duration ttl = Duration.ofMillis(ttlMillis);

            logger.info("Blacklisting token with TTL: {} seconds", ttl.getSeconds());

            // Store token in Redis with TTL
            return redisTemplate.opsForValue()
                    .set(key, "blacklisted", ttl)
                    .doOnSuccess(result -> logger.info("Token blacklisted successfully"))
                    .doOnError(error -> logger.error("Failed to blacklist token", error))
                    .onErrorReturn(false);

        } catch (Exception e) {
            logger.error("Error blacklisting token", e);
            return Mono.just(false);
        }
    }

    /**
     * Check if a token is blacklisted
     *
     * @param token JWT token to check
     * @return Mono<Boolean> true if token is blacklisted
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        
        return redisTemplate.hasKey(key)
                .doOnSuccess(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        logger.warn("Token is blacklisted");
                    }
                })
                .onErrorReturn(false);
    }

    /**
     * Remove a token from blacklist (mainly for testing purposes)
     *
     * @param token JWT token to remove from blacklist
     * @return Mono<Boolean> true if successfully removed
     */
    public Mono<Boolean> removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result -> logger.info("Token removed from blacklist: {}", result))
                .onErrorReturn(false);
    }
}

