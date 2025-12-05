package com.gdn.project.waroenk.cart.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkRemoveCartItemsRequestDto(
    @NotBlank(message = "User ID is required") String userId,
    @NotEmpty(message = "SKUs list cannot be empty") List<String> skus
) {}




