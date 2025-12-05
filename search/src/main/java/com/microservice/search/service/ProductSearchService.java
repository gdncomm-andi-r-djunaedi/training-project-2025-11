package com.microservice.search.service;

import com.microservice.search.dto.ProductEventDto;
import com.microservice.search.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchService {

    void indexProduct(ProductEventDto productEventDto);

    void updateProduct(ProductEventDto productEventDto);

    void deleteProduct(String skuId);

    Page<ProductResponseDto> searchProducts(String query, Pageable pageable);

    Page<ProductResponseDto> searchByCategory(String category, Pageable pageable);

    Page<ProductResponseDto> searchByBrand(String brand, Pageable pageable);
}