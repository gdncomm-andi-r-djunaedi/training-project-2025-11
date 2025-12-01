package com.example.product.service.impl;

import com.example.product.dto.ProductDetailResponse;
import com.example.product.dto.ProductSummaryResponse;
import com.example.product.entity.Product;
import com.example.product.exception.ProductNotFoundException;
import com.example.product.mapper.ProductMapper;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository repo;

  @Override
  public Page<ProductSummaryResponse> search(String q, int page, int size) {
    var pageable = PageRequest.of(page, size);

    Page<Product> result;
    if (q == null || q.isBlank()) {
      result = repo.findAll(pageable);
    } else {
      result = repo.findByNameContainingIgnoreCase(q, pageable);
    }

    return result.map(ProductMapper::toSummary);
  }

  @Override
  public ProductDetailResponse getById(String id) {
    Product p = repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    return ProductMapper.toDetail(p);
  }
}