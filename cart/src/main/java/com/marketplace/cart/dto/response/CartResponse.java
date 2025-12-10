package com.marketplace.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for cart information
 * Used instead of exposing the Cart entity directly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID id;
    private UUID userId; // Changed from username to userId

    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    private BigDecimal totalAmount;
    private Integer totalItems;

    /**
     * Calculate total amount from cart items
     */
    public BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total number of items
     */
    public Integer calculateTotalItems() {
        return items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
    }
}
