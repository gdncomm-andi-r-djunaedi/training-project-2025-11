package com.blibi.cart.repository;

import com.blibi.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Cart entity data access operations.
 * 
 * This interface extends MongoRepository to provide CRUD operations
 * for Cart entities in MongoDB. Spring Data MongoDB automatically
 * implements this interface at runtime.
 * 
 * Key Features:
 * - Provides standard CRUD operations (save, findById, delete, etc.)
 * - Uses UUID as the entity ID type
 * - No custom query methods needed (using default methods)
 * 
 * Available Methods (inherited from MongoRepository):
 * - save(Cart): Save or update a cart
 * - findById(UUID): Find cart by userId
 * - deleteById(UUID): Delete cart by userId
 * - existsById(UUID): Check if cart exists
 * - findAll(): Get all carts
 * 
 * @Repository: Marks this interface as a repository component
 * 
 * @author Cart Service Team
 * @version 1.0
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, UUID> {
    // No custom methods needed - using default MongoRepository methods
    // Can add custom query methods here if needed in the future
}
