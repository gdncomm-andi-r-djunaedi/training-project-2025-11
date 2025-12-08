package com.blibi.cart.service;

import com.blibi.cart.dto.AddToCartRequest;
import com.blibi.cart.dto.CartResponseDTO;

import java.util.UUID;

/**
 * Service interface for cart operations.
 * 
 * This interface defines the contract for cart-related business logic.
 * Implementations handle cart management, product integration, and data conversion.
 * 
 * The service layer separates business logic from the controller and repository layers,
 * following the layered architecture pattern.
 * 
 * @author Cart Service Team
 * @version 1.0
 */
public interface CartService {

    /**
     * Adds a product to the user's cart or updates quantity if product already exists.
     * 
     * Business Logic:
     * - Creates a new cart if user doesn't have one
     * - Fetches product details from Product Service via Feign Client
     * - Updates quantity if product already exists in cart
     * - Adds new item if product doesn't exist in cart
     * - Recalculates total cart price
     * 
     * @param userId Unique identifier for the user
     * @param request Contains productId and quantity to add
     * @return CartResponseDTO with updated cart information
     * @throws RuntimeException if product not found or price unavailable
     */
    CartResponseDTO addToCart(UUID userId, AddToCartRequest request);
    
    /**
     * Retrieves the complete cart for a user.
     * 
     * Business Logic:
     * - Fetches cart from database
     * - Converts entity to DTO for response
     * 
     * @param userId Unique identifier for the user
     * @return CartResponseDTO with cart details
     * @throws CartNotFoundException if cart doesn't exist
     */
    CartResponseDTO getCart(UUID userId);
    
    /**
     * Removes a specific product from the user's cart.
     * 
     * Business Logic:
     * - Finds and removes the product from cart items
     * - Recalculates total cart price after removal
     * 
     * @param userId Unique identifier for the user
     * @param productId Product identifier to remove
     * @return CartResponseDTO with updated cart information
     * @throws CartNotFoundException if cart doesn't exist
     */
    CartResponseDTO removeItem(UUID userId, String productId);
}

