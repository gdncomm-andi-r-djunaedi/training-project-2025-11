package com.cart.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class CartItemResponse {
    private UUID productId;
    private int quantity;
}
