package com.zasura.product.controller;

import com.zasura.product.dto.CommonResponse;
import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import com.zasura.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  //  ToDo:
  //  1. Add AOP logging
  //  2. Pagination support for search

  @Autowired
  ProductService productService;

  @GetMapping("/{productId}")
  public ResponseEntity<CommonResponse> getProductDetail(@PathVariable String productId) {
    Product productDetail = productService.getProductDetail(productId);
    HttpStatus httpStatus = productDetail == null ? HttpStatus.NOT_FOUND : HttpStatus.OK;
    return ResponseEntity.status(httpStatus)
        .body(CommonResponse.builder()
            .status(httpStatus.name())
            .code(httpStatus.value())
            .success(httpStatus.is2xxSuccessful())
            .data(productDetail)
            .build());

  }

  @PostMapping()
  public ResponseEntity<CommonResponse> createProduct(@Valid @RequestBody Product product) {
    return ResponseEntity.ok(CommonResponse.builder()
        .status(HttpStatus.OK.name())
        .code(HttpStatus.OK.value())
        .success(true)
        .data(productService.createProduct(product))
        .build());
  }

  @PostMapping("/_search")
  public ResponseEntity<CommonResponse> searchProducts(@Valid @RequestBody ProductSearchRequest productSearchRequest) {
    List<Product> products = productService.searchProducts(productSearchRequest);
    HttpStatus httpStatus = products.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
    return ResponseEntity.status(HttpStatus.OK)
        .body(CommonResponse.builder()
            .status(HttpStatus.OK.name())
            .code(HttpStatus.OK.value())
            .success(httpStatus.is2xxSuccessful())
            .data(products)
            .build());
  }
}
