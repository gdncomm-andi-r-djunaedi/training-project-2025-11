package com.blibli.product.dto;

import com.blibli.product.enums.CategoryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private CategoryType category;
    private Integer stockQuantity;
    private Date createdAt;
    private Date updatedAt;
}
