package com.marketplace.product.controller;

import com.marketplace.product.dto.PaginatedProductResponse;
import com.marketplace.product.dto.ProductIdsRequest;
import com.marketplace.product.dto.ProductRequest;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.service.ProductService;
import com.marketplace.product.util.ApiResponse;
import com.marketplace.product.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseUtil.created(response, "Product created successfully");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@RequestParam("productId") String productId) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseUtil.success(response, "Product retrieved successfully");
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(@Valid @RequestBody ProductIdsRequest request) {
        List<ProductResponse> responses = productService.getProducts(request);
        return ResponseUtil.success(responses, "Products retrieved successfully");
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir) {

        Sort sort = sortBy != null
                ? (sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending())
                : Sort.unsorted();

        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedProductResponse response = productService.getAllProducts(pageable);
        return ResponseUtil.success(response, "Products retrieved successfully");
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @RequestParam("productId") String productId,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseUtil.success(response, "Product updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteProduct(@RequestParam("productId") String productId) {
        productService.deleteProduct(productId);
        return ResponseUtil.success(productId, "Product deleted successfully");
    }

    public ResponseEntity<ApiResponse<Integer>> syncAllProductsToElasticsearch() {
        int syncedCount = productService.syncAllProductsToElasticsearch();
        return ResponseUtil.success(syncedCount, "Products synced to Elasticsearch successfully");
    }
}
