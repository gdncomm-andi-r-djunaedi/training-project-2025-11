package com.blibli.productModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private String productId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String brand;
    private Map<String, Object> attributes;
    private String imageUrl;
    private Boolean isActive = true;
}