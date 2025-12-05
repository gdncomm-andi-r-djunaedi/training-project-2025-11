package com.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateItemQuantityRequest {
    @NotNull
    private UUID productId;
    @NotNull
    private int quantity;
}
