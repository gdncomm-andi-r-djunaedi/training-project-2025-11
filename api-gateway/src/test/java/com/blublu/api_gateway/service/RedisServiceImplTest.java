package com.blublu.api_gateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisServiceImplTest {

    @Mock
    private CacheService cacheService;

    private RedisServiceImpl redisService;

    private final String defaultTimeout = "30";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisService = new RedisServiceImpl(defaultTimeout);
        redisService.cacheService = cacheService; // Manually inject mock as field injection is used
    }

    @Test
    void testCreateRedisCache() {
        String key = "testKey";
        String value = "testValue";

        redisService.createRedisCache(key, value);

        verify(cacheService).set(eq(key), eq(value), eq(Long.valueOf(defaultTimeout)), eq(TimeUnit.MINUTES));
    }

    @Test
    void testFlushAllRedis() {
        redisService.flushAllRedis();

        verify(cacheService).flushAll();
    }

    @Test
    void testFindRedisByKey() {
        String key = "testKey";
        String expectedValue = "testValue";

        when(cacheService.get(eq(key), any(TypeReference.class))).thenReturn(expectedValue);

        String result = redisService.findRedisByKey(key);

        assertEquals(expectedValue, result);
        verify(cacheService).get(eq(key), any(TypeReference.class));
    }

    @Test
    void testDeleteRedisByKey() {
        String key = "testKey";

        redisService.deleteRedisByKey(key);

        verify(cacheService).delete(key);
    }
}
