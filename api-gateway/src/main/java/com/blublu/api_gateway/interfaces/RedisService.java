package com.blublu.api_gateway.interfaces;

public interface RedisService {
  void createRedisCache(String key, String value);

  void flushAllRedis();

  String findRedisByKey(String key);

  void deleteRedisByKey(String key);
}
