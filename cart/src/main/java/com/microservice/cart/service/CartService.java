package com.microservice.cart.service;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;

public interface CartService {
    CartDto getCart(Long userId);
    CartDto addItemToCart(Long userId, AddToCartRequestDto request);
    void removeItemFromCart(Long userId, Long itemId);
}
