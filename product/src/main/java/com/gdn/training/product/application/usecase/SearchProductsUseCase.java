package com.gdn.training.product.application.usecase;

import java.util.List;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.domain.specification.ProductSpecification;

public interface SearchProductsUseCase {
    List<ProductResponse> execute(ProductSpecification spec, int page, int size);
}
