package com.gdn.training.product.application.dto;

public record ProductResponse(
        String productId,
        String sellerId,
        String name,
        String description,
        String category,
        double price,
        int stock,
        String status,
        long createdAt) {

}
