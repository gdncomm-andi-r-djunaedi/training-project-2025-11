package com.example.cart.service.impl;

import com.example.cart.dto.CartItemResponse;
import com.example.cart.dto.CartResponse;
import com.example.cart.exception.ProductUnavailableException;
import com.example.cart.service.CartService;
import com.example.cart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final StringRedisTemplate redis;
  private final ProductService productService;

  private String key(Long userId) {
    return "cart:" + userId;
  }

  @Override
  public void addItem(Long userId, String productId, int qty) {
    if (!productService.exists(productId)) {
      throw new ProductUnavailableException(productId);
    }

    redis.opsForHash().increment(key(userId), productId, qty);
  }

  @Override
  public CartResponse getCart(Long userId) {
    Map<Object, Object> entries = redis.opsForHash().entries(key(userId));

    List<CartItemResponse> items = entries.entrySet()
        .stream()
        .map(e -> new CartItemResponse((String) e.getKey(), Integer.parseInt(e.getValue().toString())))
        .toList();

    int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();

    return new CartResponse(userId, items, totalItems);
  }

  @Override
  public void removeItem(Long userId, String productId) {
    redis.opsForHash().delete(key(userId), productId);
  }

  @Override
  public void clear(Long userId) {
    redis.delete(key(userId));
  }
}