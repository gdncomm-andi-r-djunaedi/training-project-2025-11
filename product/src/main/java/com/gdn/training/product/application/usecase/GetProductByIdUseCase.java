package com.gdn.training.product.application.usecase;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.domain.exception.ProductNotFoundException;

public interface GetProductByIdUseCase {
    ProductResponse execute(String productId) throws ProductNotFoundException;
}
