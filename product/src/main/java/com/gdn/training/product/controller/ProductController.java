package com.gdn.training.product.controller;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search products", description = "Search products by query string with pagination")
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Pagination information") Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    @Operation(summary = "Get product by ID", description = "Retrieve a product by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Create a new product", description = "Create a new product with the provided details")
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }
}
