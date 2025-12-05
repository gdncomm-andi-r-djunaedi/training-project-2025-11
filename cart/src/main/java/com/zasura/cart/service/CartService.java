package com.zasura.cart.service;

import com.zasura.cart.entity.Cart;

public interface CartService {
  Cart getCart(String userId);

  Cart createCart(String userId);

  void deleteCart(String userId);
}
