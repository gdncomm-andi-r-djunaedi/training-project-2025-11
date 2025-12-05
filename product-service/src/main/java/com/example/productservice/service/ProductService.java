package com.example.productservice.service;

import com.example.productservice.dto.GetProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<GetProductResponse> getAllProducts(Pageable pageable);

    Page<GetProductResponse> searchProducts(String productName, Pageable pageable);

    GetProductResponse getProductDetail(String id);
}