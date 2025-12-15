package com.gdn.training.product.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDetailResponse {
    private String id;
    private String name;
    private String description;
    private Double price;
}
