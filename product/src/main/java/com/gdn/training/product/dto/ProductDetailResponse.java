package com.gdn.training.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductDetailResponse {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_name")
    private String productName;

    private Double price;

    private String description;
}
