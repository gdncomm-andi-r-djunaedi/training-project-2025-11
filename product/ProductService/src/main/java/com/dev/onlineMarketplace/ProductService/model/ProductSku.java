package com.dev.onlineMarketplace.ProductService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_skus")
public class ProductSku {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    private String productId;
    private String color;
    private String size;
    private Integer stockQuantity;
    private Double additionalPrice;
}
