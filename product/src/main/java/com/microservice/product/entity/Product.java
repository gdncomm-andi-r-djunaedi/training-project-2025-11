package com.microservice.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_category", columnList = "category"),
                @Index(name = "idx_products_brand", columnList = "brand")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer storeId = 10001;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long itemCode;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long length;

    @Column(nullable = false)
    private Long height;

    @Column(nullable = false)
    private Long width;

    @Column(nullable = false)
    private Long weight;

    @Column(nullable = false)
    private Integer dangerousLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}

