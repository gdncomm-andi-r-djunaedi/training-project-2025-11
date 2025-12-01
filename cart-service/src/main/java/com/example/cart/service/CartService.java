package com.example.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

  private final StringRedisTemplate redis;

  private String key(Long userId) {
    return "cart:" + userId;
  }

  public void addItem(Long userId, String productId, int qty) {
    redis.opsForHash().increment(key(userId), productId, qty);
  }

  public Map<Object, Object> getCart(Long userId) {
    return redis.opsForHash().entries(key(userId));
  }

  public void removeItem(Long userId, String productId) {
    redis.opsForHash().delete(key(userId), productId);
  }

  public void clear(Long userId) {
    redis.delete(key(userId));
  }
}
