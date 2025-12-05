package com.elfrida.cart.service;

import com.elfrida.cart.model.Cart;

public interface CartService {

    Cart addToCart(String memberId, String productId, Integer quantity);

    Cart getCart(String memberId);

    Cart removeItem(String memberId, String productId);
}


