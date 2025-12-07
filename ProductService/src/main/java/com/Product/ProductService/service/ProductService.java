package com.Product.ProductService.service;

import com.Product.ProductService.dto.ProductResponseDTO;
import com.Product.ProductService.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDTO saveProduct(ProductResponseDTO productResponseDTO);

    ProductResponseDTO getProductById(String id);

    Page<ProductResponseDTO> getProducts(Pageable pageable);

    Page<ProductResponseDTO> searchProducts(String keyword, Pageable pageable);
}
