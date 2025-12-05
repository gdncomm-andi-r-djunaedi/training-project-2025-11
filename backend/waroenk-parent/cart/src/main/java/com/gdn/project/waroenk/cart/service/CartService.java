package com.gdn.project.waroenk.cart.service;

import com.gdn.project.waroenk.cart.FilterCartRequest;
import com.gdn.project.waroenk.cart.MultipleCartResponse;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.CartItem;

import java.util.List;

/**
 * Service interface for cart operations.
 */
public interface CartService {
    
    /**
     * Get cart by user ID
     */
    Cart getCart(String userId);
    
    /**
     * Add single item to cart
     */
    Cart addItem(String userId, CartItem item);
    
    /**
     * Add multiple items to cart (bulk)
     */
    Cart bulkAddItems(String userId, List<CartItem> items);
    
    /**
     * Remove single item from cart
     */
    Cart removeItem(String userId, String sku);
    
    /**
     * Remove multiple items from cart (bulk)
     */
    Cart bulkRemoveItems(String userId, List<String> skus);
    
    /**
     * Update item quantity in cart
     */
    Cart updateItemQuantity(String userId, String sku, Integer quantity);
    
    /**
     * Clear entire cart
     */
    boolean clearCart(String userId);
    
    /**
     * Filter carts (admin)
     */
    MultipleCartResponse filterCarts(FilterCartRequest request);
}




