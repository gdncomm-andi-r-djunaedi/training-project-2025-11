package org.edmund.cart.services;

import org.edmund.cart.response.CartResponse;

public interface CartService {
    void addItem(Long userId, String productSku, int qty);
    CartResponse getCart(Long userId);
}
