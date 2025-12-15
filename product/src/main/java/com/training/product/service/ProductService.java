package com.training.product.service;

import com.training.product.dto.PageResponse;
import com.training.product.dto.ProductResponse;
import com.training.product.entity.Product;
import com.training.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
    Page<Product> productPage = productRepository.findAll(pageable);
    return mapToPageResponse(productPage);
  }

  public PageResponse<ProductResponse> searchProducts(String keyword, Pageable pageable) {
    Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    return mapToPageResponse(productPage);
  }

  private PageResponse<ProductResponse> mapToPageResponse(Page<Product> page) {
    return PageResponse.<ProductResponse>builder()
        .content(page.getContent().stream()
            .map(this::mapToDto)
            .toList())
        .pageNumber(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .first(page.isFirst())
        .last(page.isLast())
        .build();
  }

  private ProductResponse mapToDto(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }
}
