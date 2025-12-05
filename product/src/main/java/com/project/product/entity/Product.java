package com.project.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="products")
public class Product {
    @Id
    private String id;
    @Indexed(unique = true)
    private String sku;
    private String name;
    @Indexed
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    @Indexed
    private String category;
    private List<String> tags = new ArrayList<>();
    private ProductImage images;
    @Builder.Default
    private Integer viewCount = 0;
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Builder.Default
    private Integer reviewCount = 0;

    @Builder.Default
    @Indexed
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
