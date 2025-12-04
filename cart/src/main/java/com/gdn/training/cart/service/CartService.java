package com.gdn.training.cart.service;

import com.gdn.training.cart.dto.AddToCartRequest;
import com.gdn.training.cart.dto.CartResponse;
import com.gdn.training.cart.entity.Cart;

public interface CartService {
    Cart addToCart(String username, AddToCartRequest request);

    CartResponse viewCart(String username);

    Cart deleteProductFromCart(String username, String productId);
}
