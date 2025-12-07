package com.example.cart.service;

import com.example.cart.dto.AddToCartRequestDTO;
import com.example.cart.dto.CartResponseDTO;

public interface CartService {
    CartResponseDTO getCart(String userId);

    String addToCartOrUpdateQuantity(String userId, AddToCartRequestDTO addToCartRequestDTO);

    String removeItemFromCart(String userId, Long productId);

    String emptyCart(String userId);
}
