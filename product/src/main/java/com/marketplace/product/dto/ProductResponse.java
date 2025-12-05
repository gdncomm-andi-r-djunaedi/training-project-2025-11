package com.marketplace.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
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
    private ProductSpecsDto specs;
    private Double rating;
    private Integer reviewCount;
    private Boolean active;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSpecsDto {
        private String color;
        private String size;
        private String weight;
        private String material;
        private Map<String, String> additionalSpecs;
    }
}

