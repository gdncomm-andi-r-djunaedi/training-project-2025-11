package com.gdn.training.cart.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID memberId,
        Instant createdAt,
        Instant updatedAt,
        List<CartItemResponse> items) {
    public static CartResponse emptyForMember(UUID memberId) {
        return new CartResponse(
                UUID.randomUUID(),
                memberId,
                Instant.now(),
                Instant.now(),
                List.of());
    }
}
