package com.microservice.cart.service;

import com.microservice.cart.dto.AddToCartRequestDto;
import com.microservice.cart.dto.CartDto;

public interface CartService {
    CartDto getCart(Long userId);
    String addItemToCart(Long userId, AddToCartRequestDto request);
    void removeItemFromCart(Long userId, String itemId);
    String updateItemQuantity(Long userId, String itemSku, Integer quantity, String operation);
}