package com.marketplace.cart.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private String productId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
}
