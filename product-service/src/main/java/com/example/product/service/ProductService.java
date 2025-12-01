package com.example.product.service;

import com.example.product.dto.ProductDetailResponse;
import com.example.product.dto.ProductSummaryResponse;
import org.springframework.data.domain.Page;

public interface ProductService {

  Page<ProductSummaryResponse> search(String q, int page, int size);

  ProductDetailResponse getById(String id);
}