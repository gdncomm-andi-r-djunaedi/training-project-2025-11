package com.example.product.dto;

import com.example.product.model.ProductTag;

import java.time.Instant;
import java.util.List;

public record ProductDetailResponse(
    String id,
    String name,
    String description,
    double price,
    List<String> categories,
    List<ProductTag> tags,
    Instant createdAt
) {}