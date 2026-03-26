package com.blibli.productModule.service;

import com.blibli.productModule.dto.ProductDto;
import com.blibli.productModule.dto.ProductSearchResponseDto;

public interface ProductService {
    ProductDto createProduct(ProductDto request);

    ProductSearchResponseDto searchProducts(String searchTerm, String category, int page, int size);

    ProductSearchResponseDto getAllProducts(String category, int page, int size);

    ProductDto getProductById(String productId);

    void deleteProduct(String productId);
}
