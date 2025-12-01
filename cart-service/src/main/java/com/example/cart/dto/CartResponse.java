package com.example.cart.dto;

import java.util.List;

public record CartResponse(
    Long userId,
    List<CartItemResponse> items,
    int totalItems
) {}
