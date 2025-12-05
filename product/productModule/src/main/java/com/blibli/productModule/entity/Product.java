package com.blibli.productModule.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;
    @Indexed(unique = true)
    private String productId;
    @TextIndexed
    private String name;
    @TextIndexed
    private String description;
    @Indexed
    private String category;
    private BigDecimal price;
    private String brand;
    private Map<String, Object> attributes;
    private String imageUrl;
    @Indexed
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;

}
