package com.gdn.product.entity;

import com.gdn.product.ProductApplication;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Product.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    public static final String COLLECTION_NAME ="products" ;
    @Id
    private String id;
    private String productId;
    private String productName;
    private double price ;
    private String description;
    private String brand;
    private String category;

}
