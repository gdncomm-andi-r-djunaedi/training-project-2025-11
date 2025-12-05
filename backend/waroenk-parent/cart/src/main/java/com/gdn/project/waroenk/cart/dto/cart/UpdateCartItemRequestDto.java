package com.gdn.project.waroenk.cart.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotBlank(message = "SKU is required") String sku,
    @NotNull(message = "Quantity is required") @Min(value = 0, message = "Quantity must be 0 or greater") Integer quantity
) {}




