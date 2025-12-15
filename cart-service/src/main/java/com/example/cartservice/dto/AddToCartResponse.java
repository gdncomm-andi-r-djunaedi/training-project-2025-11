package com.example.cartservice.dto;

public record AddToCartResponse(
        String id,
        String memberId,
        String productId,
        Integer qty
) {}