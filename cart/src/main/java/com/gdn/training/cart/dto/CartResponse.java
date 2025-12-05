package com.gdn.training.cart.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CartResponse {
    private UUID id;
    private String memberId;
    private List<CartItemResponse> cartItems;
}
