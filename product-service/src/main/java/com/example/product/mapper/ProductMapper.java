package com.example.product.mapper;

import com.example.product.dto.ProductDetailResponse;
import com.example.product.dto.ProductSummaryResponse;
import com.example.product.entity.Product;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ProductMapper {

  public static ProductSummaryResponse toSummary(Product p) {
    if (p == null) {
      return null;
    }
    return new ProductSummaryResponse(p.getId(), p.getName(), p.getPrice(), p.getCategories(), p.getTags());
  }

  public static ProductDetailResponse toDetail(Product p) {
    if (p == null) {
      return null;
    }
    return new ProductDetailResponse(p.getId(),
        p.getName(),
        p.getDescription(),
        p.getPrice(),
        p.getCategories(),
        p.getTags(),
        p.getCreatedAt());
  }
}