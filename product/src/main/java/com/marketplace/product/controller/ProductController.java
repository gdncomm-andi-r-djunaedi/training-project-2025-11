package com.marketplace.product.controller;

import com.marketplace.product.dto.PagedResponse;
import com.marketplace.product.dto.ProductDto;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductDto>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("Search request - query: {}, page: {}, size: {}", q, page, size);
        PagedResponse<ProductDto> response = productService.searchProducts(q, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<ProductDto>> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("Get all products - page: {}, size: {}", page, size);
        PagedResponse<ProductDto> response = productService.getAllProducts(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        log.info("Get product by ID: {}", id);
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<PagedResponse<ProductDto>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("Get products by category: {}, page: {}, size: {}", category, page, size);
        PagedResponse<ProductDto> response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }
}
