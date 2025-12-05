package com.gdn.project.waroenk.cart.dto.cart;

import jakarta.validation.constraints.NotBlank;

public record RemoveCartItemRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotBlank(message = "SKU is required") String sku
) {}




