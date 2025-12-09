package com.gdn.training.product.dto;

import lombok.Data;

@Data
public class SearchProductRequest {
    private String productName;
    private int page;
    private int size;
}
