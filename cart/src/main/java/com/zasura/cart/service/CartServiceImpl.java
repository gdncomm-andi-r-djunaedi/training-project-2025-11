package com.zasura.cart.service;

import com.zasura.cart.entity.Cart;
import com.zasura.cart.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
  @Autowired
  CartRepository cartRepository;

  @Override
  public Cart getCart(String userId) {
    return cartRepository.findByUserId(getUserId(userId));
  }

  public Cart createCart(String userId) {
    return cartRepository.save(Cart.builder().userId(getUserId(userId)).build());
  }

  @Override
  public void deleteCart(String userId) {
    cartRepository.deleteByUserId(getUserId(userId));
  }

  private UUID getUserId(String userId) {
    return UUID.fromString(userId);
  }
}
