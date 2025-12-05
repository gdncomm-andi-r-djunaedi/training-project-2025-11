package com.blibli.product.entity;

import com.blibli.product.enums.CategoryType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    @Indexed
    private String name;

    private String description;

    private BigDecimal price;

    @Indexed
    private CategoryType category;

    private Integer stockQuantity;

    private Date createdAt;
    private Date updatedAt;

    @Builder.Default
    private Boolean isActive = true;
}
