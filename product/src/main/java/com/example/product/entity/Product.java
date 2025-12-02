package com.example.product.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;


@Data
@Document(collection = Product.COLLECTION_NAME)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    public static final String COLLECTION_NAME = "products";

    @Id
    private String id;

    @Indexed(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Indexed
    private String title;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    @Indexed
    private String category;
}
