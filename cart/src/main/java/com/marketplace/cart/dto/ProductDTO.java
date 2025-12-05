package com.marketplace.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO representing product data fetched from product-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private List<String> images;
    private List<String> tags;
    private Double rating;
    private Integer reviewCount;
    private Boolean active;

    /**
     * Get the first image URL or null
     */
    public String getFirstImage() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }
}

