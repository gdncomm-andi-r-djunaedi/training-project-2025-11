package com.project.cart.service;

import com.project.cart.dto.request.AddCartItemRequest;
import com.project.cart.dto.request.UpdateCartItemRequest;
import com.project.cart.dto.response.CartCountResponse;
import com.project.cart.dto.response.CartResponse;

/**
 * Service interface for Cart operations
 */
public interface CartService {

    /**
     * Add item to cart
     */
    CartResponse addItem(String userId, AddCartItemRequest request);

    /**
     * Update item quantity in cart
     */
    CartResponse updateItemQuantity(String userId, String productId, UpdateCartItemRequest request);

    /**
     * Remove item from cart
     */
    CartResponse removeItem(String userId, String productId);

    /**
     * Get user's cart
     */
    CartResponse getCart(String userId);

    /**
     * Clear entire cart
     */
    void clearCart(String userId);

    /**
     * Get cart item count
     */
    CartCountResponse getCartCount(String userId);
}
