package com.gdn.training.product.dto;

import lombok.Data;

@Data
public class ProductListRequest {
    private String productId;
    private String productName;
    private int page;
    private int size;
}
