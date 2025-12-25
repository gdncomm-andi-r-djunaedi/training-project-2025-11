package com.blibi.cart.dto;

import lombok.*;

import java.util.List;

/**
 * Data Transfer Object (DTO) for cart response.
 * 
 * This DTO is used to send cart information to API clients.
 * It contains the total cart price and a list of cart items.
 * 
 * Key Features:
 * - Used as response data in all cart endpoints
 * - Wrapped in GenericResponse for consistent API structure
 * - Separates API contract from internal entity structure
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
public class CartResponseDTO {
    
    /**
     * Total price of all items in the cart.
     * 
     * This is the sum of all CartItemDTO.price values.
     * Calculated and maintained for quick access.
     */
    private double totalCartPrice;
    
    /**
     * List of items in the cart.
     * 
     * Each CartItemDTO represents a product with its quantity and total price.
     * The list can be empty if the cart has no items.
     */
    private List<CartItemDTO> items;
}
