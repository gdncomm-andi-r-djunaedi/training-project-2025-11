package com.marketplace.gateway.service;

import com.marketplace.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    private TokenBlacklistService tokenBlacklistService;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate, jwtUtil);
    }

    @Test
    void blacklistToken_ValidToken_StoresInRedisWithTTL() {
        // Arrange
        String token = "valid.jwt.token";
        long futureTimeMillis = System.currentTimeMillis() + 3600000; // 1 hour from now
        Date expiration = new Date(futureTimeMillis);
        String expectedKey = BLACKLIST_PREFIX + token;

        when(jwtUtil.extractExpiration(token)).thenReturn(expiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(eq(expectedKey), eq("blacklisted"), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = tokenBlacklistService.blacklistToken(token);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(jwtUtil).extractExpiration(token);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq(expectedKey), eq("blacklisted"), any(Duration.class));
    }

    @Test
    void blacklistToken_ExpiredToken_ReturnsTrueWithoutStoring() {
        // Arrange
        String token = "expired.jwt.token";
        long pastTimeMillis = System.currentTimeMillis() - 3600000; // 1 hour ago
        Date expiration = new Date(pastTimeMillis);

        when(jwtUtil.extractExpiration(token)).thenReturn(expiration);

        // Act
        Mono<Boolean> result = tokenBlacklistService.blacklistToken(token);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(jwtUtil).extractExpiration(token);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void blacklistToken_JwtParseError_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.token";

        when(jwtUtil.extractExpiration(invalidToken))
                .thenThrow(new RuntimeException("Failed to parse JWT"));

        // Act
        Mono<Boolean> result = tokenBlacklistService.blacklistToken(invalidToken);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void blacklistToken_RedisError_PropagatesError() {
        // Arrange
        String token = "valid.jwt.token";
        long futureTimeMillis = System.currentTimeMillis() + 3600000;
        Date expiration = new Date(futureTimeMillis);
        String expectedKey = BLACKLIST_PREFIX + token;

        when(jwtUtil.extractExpiration(token)).thenReturn(expiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(eq(expectedKey), eq("blacklisted"), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("Redis connection failed")));

        // Act
        Mono<Boolean> result = tokenBlacklistService.blacklistToken(token);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(redisTemplate).opsForValue();
    }

    @Test
    void isBlacklisted_TokenExists_ReturnsTrue() {
        // Arrange
        String token = "blacklisted.jwt.token";
        String expectedKey = BLACKLIST_PREFIX + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = tokenBlacklistService.isBlacklisted(token);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    void isBlacklisted_TokenNotExists_ReturnsFalse() {
        // Arrange
        String token = "valid.jwt.token";
        String expectedKey = BLACKLIST_PREFIX + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = tokenBlacklistService.isBlacklisted(token);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    void blacklistToken_TokenExpiresInOneSecond_StoresWithCorrectTTL() {
        // Arrange
        String token = "soon-expiring.jwt.token";
        long futureTimeMillis = System.currentTimeMillis() + 1000; // 1 second from now
        Date expiration = new Date(futureTimeMillis);
        String expectedKey = BLACKLIST_PREFIX + token;

        when(jwtUtil.extractExpiration(token)).thenReturn(expiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(eq(expectedKey), eq("blacklisted"), any(Duration.class)))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = tokenBlacklistService.blacklistToken(token);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(valueOperations).set(eq(expectedKey), eq("blacklisted"), any(Duration.class));
    }
}

