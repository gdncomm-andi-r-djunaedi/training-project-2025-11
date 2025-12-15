package com.blibli.cart.service;

import com.blibli.cart.dto.AddToCartRequest;
import com.blibli.cart.dto.CartResponseDTO;


public interface CartService {

    CartResponseDTO addToCart(String userId, AddToCartRequest request);

    CartResponseDTO getCarts(String userId);

    CartResponseDTO removeItemFromCart(String userId, String productId);

    void clearCart(String userId);
}
