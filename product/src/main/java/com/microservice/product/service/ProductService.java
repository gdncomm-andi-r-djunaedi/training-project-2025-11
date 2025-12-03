package com.microservice.product.service;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.dto.ProductSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponseDto> getProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsBySearch(String searchTerm, Pageable pageable);

    Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto);

    ProductResponseDto getProductsById(Long id);

    ProductResponseDto addProduct(ProductDto productDto);

    ProductResponseDto updateProduct(Long id, ProductDto productDto);

    void deleteById(Long id);
}
