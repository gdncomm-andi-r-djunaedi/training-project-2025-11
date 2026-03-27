package com.blibli.product.dto;

import com.blibli.product.enums.CategoryType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private CategoryType category;
    private Integer stockQuantity;
    private String eventType;
}
