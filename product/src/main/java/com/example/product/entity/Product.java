package com.example.product.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
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
    private long productId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private boolean markForDelete;
}
