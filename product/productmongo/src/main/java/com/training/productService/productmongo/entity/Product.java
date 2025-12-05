package com.training.productService.productmongo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = Product.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product implements Serializable {

    public static final String COLLECTION_NAME = "products";

    @Id
    private String id;
    private String sku;
    private String name;
    private String description;
    private Double price;
    private String category;
    private List<String> tags;
    private List<String> images;
}
