package com.demo.cart.service;

import com.demo.cart.DTO.AddItemRequestDTO;
import com.demo.cart.DTO.CartResponseDTO;
import com.demo.cart.DTO.DecreaseQuantityRequestDTO;
import com.demo.cart.DTO.UpdateItemRequestDTO;

public interface CartService {
//    CartResponseDTO addToCart(Long userId, AddItemRequestDTO request);
    void addToCart(Long userId, AddItemRequestDTO request);
    CartResponseDTO getCart(Long userId);
    CartResponseDTO updateCart(Long userId, UpdateItemRequestDTO request);
    void removeItem(Long userId, String cartItemId);
    CartResponseDTO decreaseQuantity(Long userId, DecreaseQuantityRequestDTO request);
}
