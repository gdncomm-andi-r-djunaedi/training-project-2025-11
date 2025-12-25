package com.kailash.cart.service;

import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.CartResponse;
import com.kailash.cart.entity.Cart;
import org.springframework.stereotype.Service;


public interface CartService {
    ApiResponse<CartResponse> getCart(String memberId);
    ApiResponse<CartResponse> addOrUpdateItem(String memberId, String sku, int qty);
    ApiResponse<CartResponse> removeItem(String memberId, String sku);
}
