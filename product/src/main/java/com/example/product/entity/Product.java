package com.example.product.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Product.COLLECTION_NAME)
@CompoundIndex(name = "product_name_idx", def = "{'productName': 1}", unique = false)
@CompoundIndex(name = "product_name_and_category_idx", def = "{'productName': 1, 'category': 1}", unique = false)
public class Product {
    public static final String COLLECTION_NAME = "PRODUCT";

    @Id
    private ObjectId productId;
    private String productName;
    private String description;
    private Double price;
    private String category;
    private List<String> images;
}
