package com.cart.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CartResponse {
    private UUID id;
    private UUID customerId;
    private List<CartItemResponse> items;
}
