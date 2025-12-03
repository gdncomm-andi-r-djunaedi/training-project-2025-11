package com.gdn.training.api_gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.gdn.training.api_gateway.config.RateLimiterProperties;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        RateLimiterProperties properties = new RateLimiterProperties();
        properties.setRequestsPerMinute(2);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        rateLimiterService = new RateLimiterService(properties, redisTemplate);
    }

    @Test
    void consumesTokensWhileWithinLimit() {
        when(valueOperations.increment("ratelimit:client-1")).thenReturn(1L, 2L);
        when(redisTemplate.getExpire(eq("ratelimit:client-1"), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(50000L, 40000L);

        RateLimitResult first = rateLimiterService.consume("client-1");
        RateLimitResult second = rateLimiterService.consume("client-1");

        assertThat(first.allowed()).isTrue();
        assertThat(first.remainingTokens()).isEqualTo(1);

        assertThat(second.allowed()).isTrue();
        assertThat(second.remainingTokens()).isZero();

        verify(redisTemplate, times(1)).expire(eq("ratelimit:client-1"), eq(Duration.ofMinutes(1)));
    }

    @Test
    void rejectsWhenLimitExceeded() {
        when(valueOperations.increment("ratelimit:client-1")).thenReturn(1L, 2L, 3L);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.MILLISECONDS))).thenReturn(50000L);

        rateLimiterService.consume("client-1");
        rateLimiterService.consume("client-1");
        RateLimitResult blocked = rateLimiterService.consume("client-1");

        assertThat(blocked.allowed()).isFalse();
        assertThat(blocked.remainingTokens()).isZero();

        verify(redisTemplate, times(3)).getExpire(anyString(), eq(TimeUnit.MILLISECONDS));
        verify(redisTemplate, times(1)).expire(anyString(), eq(Duration.ofMinutes(1)));
    }
}

