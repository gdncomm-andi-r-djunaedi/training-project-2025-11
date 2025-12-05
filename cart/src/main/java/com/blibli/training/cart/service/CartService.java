package com.blibli.training.cart.service;

import com.blibli.training.cart.dto.CartResponse;

public interface CartService {
    CartResponse addToBag(String username, String productName, int quantity);
    CartResponse getAllCart(String username);
}
