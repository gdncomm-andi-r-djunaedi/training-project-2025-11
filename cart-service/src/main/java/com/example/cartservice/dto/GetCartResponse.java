package com.example.cartservice.dto;

import java.math.BigDecimal;

public record GetCartResponse(
        String id,
        String memberId,
        String productId,
        String productName,
        Integer qty,
        BigDecimal totalPrice
) {}