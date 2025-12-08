package com.kailash.product.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIndex {

    @Id
    private String id;
    private String sku;
    private String name;
    private String shortDescription;
    private Double price;
}
