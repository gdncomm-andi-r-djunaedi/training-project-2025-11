package com.gdn.faurihakim.member.command.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheCommandImpl {

  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;

  public CacheCommandImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.objectMapper = objectMapper;
  }

  // Create/Update - set value without expiration
  public <T> void set(String key, T value) {
    try {
      String valueString = this.objectMapper.writeValueAsString(value);
      stringRedisTemplate.opsForValue().set(key, valueString);
    } catch (Exception e) {
      System.out.println("Error when setting cache: " + e.getMessage());
    }
  }

  // Create/Update - set value with expiration
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

  // Delete - remove specific key
  public Boolean delete(String key) {
    try {
      return stringRedisTemplate.delete(key);
    } catch (Exception e) {
      System.out.println("Error when deleting cache: " + e.getMessage());
    }
    return false;
  }

  // Check if key exists
  public Boolean hasKey(String key) {
    return stringRedisTemplate.hasKey(key);
  }

  // Flush all - delete all keys in all databases
  public void flushAll() {
    stringRedisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  // Flush current database only
  public void flushDb() {
    stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
  }
}