package com.example.cartservice.dto;

import java.math.BigDecimal;

public record GetProductResponse(
        String id,
        String name,
        String description,
        String category,
        BigDecimal price
) {}