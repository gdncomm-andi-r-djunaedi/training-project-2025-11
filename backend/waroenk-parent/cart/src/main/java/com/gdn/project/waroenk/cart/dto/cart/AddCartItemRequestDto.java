package com.gdn.project.waroenk.cart.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AddCartItemRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotBlank(message = "SKU is required") String sku,
    @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
    Long priceSnapshot,
    String title,
    String imageUrl,
    Map<String, String> attributes
) {}




