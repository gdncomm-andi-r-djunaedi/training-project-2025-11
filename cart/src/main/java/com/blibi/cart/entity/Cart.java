package com.blibi.cart.entity;

import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

/**
 * Cart entity representing a user's shopping cart in MongoDB.
 * 
 * This entity maps to the "cart" collection in MongoDB and represents
 * a complete shopping cart for a user, containing multiple cart items.
 * 
 * Key Features:
 * - Uses UUID as the document ID (userId serves as cartId)
 * - Contains a list of CartItem objects (embedded documents)
 * - Maintains total cart price for quick access
 * - Uses validation constraints for data integrity
 * 
 * @Document: Maps this class to MongoDB collection
 * @Data: Generates getters, setters, toString, equals, and hashCode
 * @Builder: Provides builder pattern for object creation
 * 
 * @author HKB
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Document(collection = Cart.COLLECTION_NAME)
public class Cart {
    
    /**
     * MongoDB collection name constant.
     * Used in @Document annotation to specify collection name.
     */
    static final String COLLECTION_NAME = "cart";

    /**
     * Unique identifier for the cart.
     * 
     * This is the MongoDB document ID and also represents the userId.
     * UUID format ensures uniqueness across all carts.
     * 
     * @Id: Marks this field as the MongoDB document identifier
     */
    @Id
    private UUID cartId;
    
    /**
     * Total price of all items in the cart.
     * 
     * This value is calculated by summing all CartItem.itemTotalPrice values.
     * Maintained for quick access without recalculating each time.
     * 
     * @Positive: Validation constraint ensures price is greater than zero
     */
    @Positive(message = "Price must be greater than zero")
    private Double totalCartPrice;
    
    /**
     * List of items in the cart.
     * 
     * Each CartItem represents a product with quantity and total price.
     * This is an embedded document list in MongoDB.
     * 
     * The list can be empty (new cart) or contain multiple items.
     */
    private List<CartItem> items;
}
