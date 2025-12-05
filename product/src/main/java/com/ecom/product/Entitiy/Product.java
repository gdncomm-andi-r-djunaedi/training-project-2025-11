package com.ecom.product.Entitiy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document(collection = Product.COLLECTION_NAME)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    public static final String COLLECTION_NAME = "product";
    private String id;
    private String productSku;
    private String name;
    private Double price;
    private String image;
    private Instant createdAt;
    private String description;

    public String getProductSku() {
        return productSku;
    }
}
