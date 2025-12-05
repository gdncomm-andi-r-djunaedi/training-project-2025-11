package com.project.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product DTO for integration with Product Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private String id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String slug;
    private ImageDto images;
    private Boolean isActive;
}