package com.example.cartservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(

        @NotBlank(message = "Product id is required")
        String productId,

        @NotNull(message = "Qty id is required")
        Integer qty
) {
}