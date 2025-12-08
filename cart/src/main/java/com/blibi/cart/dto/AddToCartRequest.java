package com.blibi.cart.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for adding items to cart request.
 * 
 * This DTO is used to receive request data from the API client
 * when adding a product to the cart. It contains the minimum
 * required information to perform the add operation.
 * 
 * Key Features:
 * - Used as @RequestBody in CartController.add() method
 * - Contains product reference and quantity
 * - Can be extended with validation annotations if needed
 * 
 * @Data: Generates getters, setters, toString, equals, and hashCode
 * @Builder: Provides builder pattern for object creation
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartRequest {

    /**
     * Product identifier to add to cart.
     * 
     * This should be a valid product ID from the Product Service.
     * The service will validate this by calling Product Service.
     */
    private String productId;
    
    /**
     * Quantity of the product to add.
     * 
     * Must be a positive integer representing how many units
     * of the product the user wants to add to the cart.
     * If the product already exists in cart, this quantity
     * will be added to the existing quantity.
     */
    private int quantity;
}
