package com.gdn.project.waroenk.cart.dto.checkout;

import java.time.Instant;
import java.util.List;

public record CheckoutResponseDto(
    String checkoutId,
    String userId,
    String sourceCartId,
    List<CheckoutItemDto> items,
    Long totalAmount,
    String status,
    Instant lockedAt,
    Instant expiresAt,
    Instant createdAt
) {}




