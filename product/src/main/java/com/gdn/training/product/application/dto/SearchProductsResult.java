package com.gdn.training.product.application.dto;

import java.util.List;

public record SearchProductsResult(
        List<ProductResponse> products,
        int total,
        int page,
        int size) {

}
