package com.blibi.cart.dto;

import lombok.*;

/**
 * Data Transfer Object (DTO) for a single cart item in API responses.
 * 
 * This DTO represents a product item in the cart when sending
 * cart information to API clients. It contains essential item
 * information without exposing internal entity structure.
 * 
 * Key Features:
 * - Used in CartResponseDTO.items list
 * - Contains product reference, quantity, and total price
 * - Simplified representation compared to CartItem entity
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
@ToString
@Builder
public class CartItemDTO {
    
    /**
     * Product identifier.
     * 
     * Reference to the product in the Product Service.
     */
    private String productId;
    
    /**
     * Quantity of the product in the cart.
     * 
     * Represents how many units of this product are in the cart.
     */
    private int quantity;
    
    /**
     * Total price for this cart item.
     * 
     * Calculated as: quantity * product unit price
     * This is the total cost for this specific item.
     */
    private double price;
}
