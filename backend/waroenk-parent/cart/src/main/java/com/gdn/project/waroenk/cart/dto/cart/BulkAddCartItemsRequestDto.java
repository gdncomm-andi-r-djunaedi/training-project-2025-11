package com.gdn.project.waroenk.cart.dto.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkAddCartItemsRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotEmpty(message = "Items list cannot be empty") @Valid List<CartItemInputDto> items
) {
    public record CartItemInputDto(
        @NotBlank(message = "SKU is required") String sku,
        @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
        Long priceSnapshot,
        String title,
        String imageUrl,
        java.util.Map<String, String> attributes
    ) {}
}




