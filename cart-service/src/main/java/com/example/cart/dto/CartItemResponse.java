package com.example.cart.dto;

public record CartItemResponse(
    String productId,
    int quantity
) {}