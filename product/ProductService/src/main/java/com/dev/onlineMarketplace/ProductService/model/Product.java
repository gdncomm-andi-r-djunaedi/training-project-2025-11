package com.dev.onlineMarketplace.ProductService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    @Indexed
    private String name;

    @Indexed(unique = true)
    private String sku;

    private String description;
    private Double price;
    private String category;
    private String imageUrl;
}
