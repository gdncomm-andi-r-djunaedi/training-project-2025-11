package com.blibli.ProductService.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "products")
public class Product {

    @Id
    private String sku;
    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Double)
    private BigDecimal price;
    @Field(type = FieldType.Keyword)
    private String category;



}
