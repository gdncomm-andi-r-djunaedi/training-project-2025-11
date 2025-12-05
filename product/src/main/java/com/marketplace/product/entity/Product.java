package com.marketplace.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @TextIndexed(weight = 10)
    private String name;

    @TextIndexed(weight = 5)
    private String description;

    @Indexed
    private String category;

    @Indexed
    private String brand;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer discountPercentage;

    private List<String> images;

    private List<String> tags;

    private ProductSpecs specs;

    private Double rating;

    private Integer reviewCount;

    @Indexed
    private Boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSpecs {
        private String color;
        private String size;
        private String weight;
        private String material;
        private java.util.Map<String, String> additionalSpecs;
    }
}

