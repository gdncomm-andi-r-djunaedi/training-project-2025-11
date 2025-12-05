package com.zasura.apiGateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;

  public CacheService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.objectMapper = objectMapper;
  }

  public <T> void set(String key, T value, long timeout, TimeUnit unit) {
    try {
      String valueString = this.objectMapper.writeValueAsString(value);
      stringRedisTemplate.opsForValue().set(key, valueString, timeout, unit);
    } catch (Exception e) {
      System.out.println("Error when setting cache with expiration: " + e.getMessage());
    }
  }

  public <T> T get(String key, TypeReference<T> typeRef) {
    try {
      String redisValue = stringRedisTemplate.opsForValue().get(key);
      if (redisValue != null) {
        return objectMapper.readValue(redisValue, typeRef);
      }
    } catch (Exception e) {
      System.out.println("Error when getting cache: " + e.getMessage());
    }
    return null;
  }

  public Boolean delete(String key) {
    try {
      return stringRedisTemplate.delete(key);
    } catch (Exception e) {
      System.out.println("Error when deleting cache: " + e.getMessage());
    }
    return false;
  }

  public Boolean hasKey(String key) {
    return stringRedisTemplate.hasKey(key);
  }

  public void flushAll() {
    stringRedisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  public void flushDb() {
    stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
  }
}