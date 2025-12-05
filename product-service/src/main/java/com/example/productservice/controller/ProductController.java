package com.example.productservice.controller;

import com.example.productservice.dto.GetProductResponse;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/lists")
    public ResponseEntity<Page<GetProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GetProductResponse>> searchProducts(
            @RequestParam String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.searchProducts(productName, PageRequest.of(page, size)));
    }

    @GetMapping("/product-detail/{id}")
    public ResponseEntity<GetProductResponse> getProductDetail(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }
}