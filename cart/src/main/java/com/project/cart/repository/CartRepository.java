package com.project.cart.repository;

import com.project.cart.entity.Cart;
import com.project.cart.entity.CartStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cart entity operations
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(String userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(String userId);

    /**
     * Find carts by status
     */
    List<Cart> findByStatus(CartStatus status);

    /**
     * Find expired carts
     */
    List<Cart> findByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Delete expired carts
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Find abandoned carts (no activity for specified time)
     */
    @Query("{ 'status': 'ACTIVE', 'updatedAt': { $lt: ?0 } }")
    List<Cart> findAbandonedCarts(LocalDateTime beforeDateTime);

    /**
     * Count active carts
     */
    long countByStatus(CartStatus status);
}
