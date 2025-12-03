package com.blibli.cart.service;

import com.blibli.cart.dto.AddToCartRequest;
import com.blibli.cart.dto.CartResponse;


public interface CartService {

    CartResponse addToCart(String userId, AddToCartRequest request);

    CartResponse getCart(String userId);

    CartResponse removeFromCart(String userId, String productId);

    void clearCart(String userId);
}
