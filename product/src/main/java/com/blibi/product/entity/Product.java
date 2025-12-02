package com.blibi.product.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Document(collection = Product.COLLECTION_NAME)
public class Product {
    static final String COLLECTION_NAME = "products";

    @Id
    private ObjectId productId;
    @NotNull(message = "Product Name cannot be null")
    private String productName;
    @NotNull(message = "Product Description cannot be null")
    private String description;
    @Positive(message = "Price must be greater than zero")
    private Double price;
    @NotNull(message = "Category cannot be null")
    private String category;
    private List<String> images;
}
