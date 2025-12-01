package com.example.cart.service;

import com.example.cart.dto.CartResponse;

public interface CartService {

  void addItem(Long userId, String productId, int qty);

  CartResponse getCart(Long userId);

  void removeItem(Long userId, String productId);

  void clear(Long userId);
}