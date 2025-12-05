package com.gdn.project.waroenk.cart.dto.cart;

import java.time.Instant;
import java.util.List;

public record CartResponseDto(
    String id,
    String userId,
    List<CartItemDto> items,
    String currency,
    Long totalAmount,
    Integer totalItems,
    Integer version,
    Instant createdAt,
    Instant updatedAt
) {}




