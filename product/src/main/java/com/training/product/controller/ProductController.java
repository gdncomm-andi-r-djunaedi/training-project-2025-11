package com.training.product.controller;

import com.training.product.dto.PageResponse;
import com.training.product.dto.ProductResponse;
import com.training.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @GetMapping
  public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    PageResponse<ProductResponse> response = productService.getAllProducts(pageable);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/search")
  public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    PageResponse<ProductResponse> response = productService.searchProducts(keyword, pageable);
    return ResponseEntity.ok(response);
  }
}
