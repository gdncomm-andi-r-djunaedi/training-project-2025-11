package com.blublu.api_gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CacheServiceTest {

  @Mock
  private StringRedisTemplate stringRedisTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private RedisConnectionFactory connectionFactory;

  @Mock
  private RedisConnection connection;

  @InjectMocks
  private CacheService cacheService;

  @Test
  public void allCacheServiceTest() throws JsonProcessingException {
    String value = "value";
    String key = "key";
    when(objectMapper.writeValueAsString(value)).thenReturn(value);
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(key)).thenReturn(value);
    when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(value);

    cacheService.set(key, value);
    String result = cacheService.get(key, new TypeReference<String>() {
    });
    assertEquals(value, result);
    verify(valueOperations).set(key, value);
    verify(valueOperations).get(key);

    // Redis value null
    when(stringRedisTemplate.opsForValue().get(anyString())).thenReturn(null);
    String resultNull = cacheService.get(key, new TypeReference<String>() {
    });
    assertNull(resultNull);
  }

  @Test
  public void testSetWithExpiration() throws Exception {
    String key = "key";
    String value = "value";
    String valueString = "value";
    long timeout = 5L;
    TimeUnit unit = TimeUnit.SECONDS;

    when(objectMapper.writeValueAsString(value)).thenReturn(valueString);
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

    cacheService.set(key, value, timeout, unit);

    verify(valueOperations).set(eq(key), eq(valueString), eq(timeout), eq(unit));
  }

  @Test
  public void testDeleteAndHasKey() {
    String key = "key";

    when(stringRedisTemplate.delete(key)).thenReturn(true);
    when(stringRedisTemplate.hasKey(key)).thenReturn(true);

    Boolean deleted = cacheService.delete(key);
    Boolean hasKey = cacheService.hasKey(key);

    assertTrue(deleted);
    assertTrue(hasKey);
  }

  @Test
  public void testFlushAllAndFlushDb() {
    when(stringRedisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
    when(connectionFactory.getConnection()).thenReturn(connection);

    cacheService.flushAll();
    cacheService.flushDb();

    verify(connection).flushAll();
    verify(connection).flushDb();
  }

  @Test
  public void testDeleteReturnsFalseOnException() {
    String key = "key";
    when(stringRedisTemplate.delete(key)).thenThrow(new RuntimeException("err"));
    Boolean deleted = cacheService.delete(key);
    assertFalse(deleted);
  }

  @Test
  public void testAnyOtherException() {
    assertDoesNotThrow(() -> cacheService.set(null, null));
    assertDoesNotThrow(() -> cacheService.set(null, null, 10, TimeUnit.MINUTES));
    assertDoesNotThrow(() -> cacheService.get(null, null));
  }
}
