package com.blibi.cart.entity;

import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * CartItem entity representing a single item in a shopping cart.
 * 
 * This is an embedded document within the Cart entity in MongoDB.
 * Each CartItem represents one product with its quantity and total price.
 * 
 * Key Features:
 * - Stores product reference (productId)
 * - Tracks quantity of the product
 * - Maintains calculated total price (quantity * unit price)
 * - Uses validation constraints for data integrity
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
public class CartItem {
    
    /**
     * Product identifier referencing a product in the Product Service.
     * 
     * This is a string reference to the product, not a foreign key
     * (since we're using microservices architecture).
     * The actual product details are fetched from Product Service when needed.
     */
    private String productId;
    /**
     * Quantity of the product in the cart.
     * 
     * Represents how many units of this product the user wants.
     * Must be a positive integer (greater than 0).
     * 
     * @Positive: Validation constraint ensures quantity is positive
     */
    @Positive(message =  "Quantity should not be less than 0")
    private int quantity;
    
    /**
     * Total price for this cart item.
     * 
     * Calculated as: quantity * product unit price
     * This value is stored to avoid recalculating on every access.
     * Updated whenever quantity changes or product price changes.
     * 
     * @Positive: Validation constraint ensures price is greater than zero
     */
    @Positive(message = "Price must be greater than zero")
    private double itemTotalPrice;
}
