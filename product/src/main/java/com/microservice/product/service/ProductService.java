package com.microservice.product.service;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductResponseDto> getProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsBySearch(String searchTerm, Pageable pageable);

    ProductResponseDto getProductsById(String skuId);  // Changed from Long to String

    ProductResponseDto addProduct(ProductDto productDto);

    ProductResponseDto updateProduct(String skuId, ProductDto productDto);  // Changed from Long to String

    void deleteById(String skuId);  // Changed from Long to String

    Boolean isProductIdPresent(String skuId);  // Changed from Long to String

    List<ProductResponseDto> getProductsBySkuIds(List<String> skuIds);
}