package com.Product.ProductService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(Product.TABLE_NAME)
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    public static final String TABLE_NAME = "Product";

    @Id
    private String productId;
    private String productCode;
    private String productName;
    private String productDescription;
    private double price;
    private String category;
    private int stockQuantity;
}
