package com.gdn.training.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductDetailResponse {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("stock")
    private Integer stock;
}
