package com.gdn.project.waroenk.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkout entity - represents validated/locked items for a short time.
 * Primary storage is Redis with optional MongoDB persistence for audit.
 * Has a TTL index on expiresAt for automatic cleanup.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "checkout_items")
public class Checkout {
    @Id
    private String id;

    @Indexed(unique = true)
    private String checkoutId;

    @Indexed
    private String userId;

    private String sourceCartId;

    @Builder.Default
    private List<CheckoutItem> items = new ArrayList<>();

    private Long totalAmount;

    @Builder.Default
    private String status = "PENDING"; // PENDING, RESERVED, EXPIRED, FINALIZED, CANCELLED

    private Instant lockedAt;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;

    /**
     * Calculate total amount
     */
    public Long calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            return 0L;
        }
        return items.stream()
                .mapToLong(item -> (item.getPriceSnapshot() != null ? item.getPriceSnapshot() : 0L)
                        * (item.getQuantity() != null ? item.getQuantity() : 0))
                .sum();
    }

    /**
     * Check if checkout has expired
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if all items are reserved
     */
    public boolean isFullyReserved() {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.stream().allMatch(item -> Boolean.TRUE.equals(item.getReserved()));
    }
}




