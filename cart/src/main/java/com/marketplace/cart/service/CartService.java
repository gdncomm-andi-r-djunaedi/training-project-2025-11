package com.marketplace.cart.service;

import com.marketplace.cart.dto.AddItemRequest;
import com.marketplace.cart.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String userId);
    CartResponse addItem(String userId, AddItemRequest request, String action);
    CartResponse removeItem(String userId, String productId);
}
