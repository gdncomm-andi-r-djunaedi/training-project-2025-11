package com.zasura.product.controller;

import com.zasura.product.dto.CommonResponse;
import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import com.zasura.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
  @Autowired
  ProductService productService;

  @GetMapping("/{productId}")
  public ResponseEntity<CommonResponse> getProductDetail(@PathVariable String productId) {
    return ResponseEntity.ok()
        .body(CommonResponse.success(productService.getProductDetail(productId)));
  }

  @GetMapping("/_migrate")
  public ResponseEntity<CommonResponse> migrate() {
    return ResponseEntity.ok()
        .body(CommonResponse.success(productService.migrate()));
  }

  @PostMapping()
  public ResponseEntity<CommonResponse<Product>> createProduct(@Valid @RequestBody Product product) {
    return ResponseEntity.ok(CommonResponse.success(productService.createProduct(product)));
  }

  @PostMapping("/_search")
  public ResponseEntity<CommonResponse<List<Product>>> searchProducts(@Valid @RequestBody ProductSearchRequest productSearchRequest) {
    Page<Product> products = productService.searchProducts(productSearchRequest);
    return ResponseEntity.ok(CommonResponse.successWithPagination(products.stream().toList(),
        productSearchRequest.getPagination()));
  }
}
