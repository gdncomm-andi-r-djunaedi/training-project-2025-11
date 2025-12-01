package com.example.product.controller;

import com.example.product.dto.ProductDetailResponse;
import com.example.product.dto.ProductSummaryResponse;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<Page<ProductSummaryResponse>> list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "q", required = false) String q
  ) {
    return ResponseEntity.ok(productService.search(q, page, size));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDetailResponse> detail(
      @PathVariable("id") String id
  ) {
    return ResponseEntity.ok(productService.getById(id));
  }
}