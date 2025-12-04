package com.kailash.product.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;      // Mongo ObjectId
    private String sku;     // business SKU (unique)
    private String name;
    private String shortDescription;
    private Double price;
    private Instant createdAt;

    public static Product of(String sku, String name, String shortDescription, Double price) {
        return Product.builder()
                .sku(sku)
                .name(name)
                .shortDescription(shortDescription)
                .price(price)
                .createdAt(Instant.now())
                .build();
    }
}
