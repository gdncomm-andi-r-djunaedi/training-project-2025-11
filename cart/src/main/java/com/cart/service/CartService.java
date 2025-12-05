package com.cart.service;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.dto.response.CartResponse;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;

import java.util.List;
import java.util.UUID;

public interface CartService {
    Cart getOrCreateCart(UUID customerId);

    List<CartItem> getCartItems(UUID customerId);

    CartItem addItem(UUID customerId, AddItemRequest request);

    CartItem updateItemQuantity(UUID customerId, UpdateItemQuantityRequest request);

    Cart removeItem(UUID customerId, UUID productId);

    void clearCart(UUID customerId);
}
