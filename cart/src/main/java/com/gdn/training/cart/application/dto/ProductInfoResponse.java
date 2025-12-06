package com.gdn.training.cart.application.dto;

import java.util.UUID;

public record ProductInfoResponse(
        UUID productId,
        UUID sellerId,
        String name,
        String description,
        String category,
        double price,
        int stock,
        String status,
        long createdAtmillis) {

}
