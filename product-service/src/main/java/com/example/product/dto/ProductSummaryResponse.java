package com.example.product.dto;

import com.example.product.model.ProductTag;

import java.util.List;

public record ProductSummaryResponse(
    String id,
    String name,
    double price,
    List<String> categories,
    List<ProductTag> tags
) {}
