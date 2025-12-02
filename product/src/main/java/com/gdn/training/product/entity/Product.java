package com.gdn.training.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @Field("product_sku")
    private String productSku;

    @Field("product_name")
    private String productName;

    @Field("quantity")
    private Integer quantity;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private String category;

    @org.springframework.data.annotation.CreatedDate
    @Field("created_time")
    private java.util.Date createdTime;
}
