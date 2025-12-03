package com.blublu.api_gateway.service;

import com.blublu.api_gateway.interfaces.RedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

  private final Long DEFAULT_REDIS_TIMEOUT;

  @Autowired
  CacheService cacheService;

  public RedisServiceImpl(@Value("${redis.expiryInMinutes}") String defaultRedisTimeout) {
    this.DEFAULT_REDIS_TIMEOUT = Long.valueOf(defaultRedisTimeout);
  }

  @Override
  public void createRedisCache(String key, String value) {
    cacheService.set(key, value, DEFAULT_REDIS_TIMEOUT, TimeUnit.MINUTES);
  }

  @Override
  public void flushAllRedis() {
    cacheService.flushAll();
  }

  @Override
  public String findRedisByKey(String key) {
    return cacheService.get(key, new TypeReference<>() {
    });
  }

  @Override
  public void deleteRedisByKey(String key) {
    cacheService.delete(key);
  }
}
