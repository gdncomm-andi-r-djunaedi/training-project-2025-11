package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartResponse;

public interface CartService {
    CartResponse addToCart(String username, AddToCartRequest request);

    CartResponse viewCart(String username);

    CartResponse deleteProductFromCart(String username, String productId);
}
