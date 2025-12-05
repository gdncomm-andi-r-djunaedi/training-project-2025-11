package com.blibli.gdn.productService.service;

import com.blibli.gdn.productService.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchService {
    Page<ProductResponse> searchProducts(String name, String category, Pageable pageable, String sort);
}

