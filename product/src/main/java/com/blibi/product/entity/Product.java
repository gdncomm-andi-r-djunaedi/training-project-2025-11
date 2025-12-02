package com.blibi.product.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@CompoundIndex(name = "product_name_idx",def = "{'productName'= 1}",unique = false)
@Document(collection = Product.COLLECTION_NAME)
public class Product {
    static final String COLLECTION_NAME = "products";

    @Id
    private ObjectId productId;
    @NotNull(message = "Product Name cannot be Empty")
    private String productName;
    @NotNull(message = "Product Description cannot be Empty")
    private String description;
    @Positive(message = "Price must be greater than zero")
    private Double price;
    @NotNull(message = "Category cannot be Empty")
    private String category;
    private List<String> images;
}
