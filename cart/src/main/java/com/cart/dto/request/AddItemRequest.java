package com.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AddItemRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private int quantity;
    @NotNull
    private BigDecimal priceEach;
    @NotNull
    private String productName;
}
