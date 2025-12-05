package com.blibli.product.controller;

import com.blibli.product.dto.ApiResponse;
import com.blibli.product.dto.PageResponse;
import com.blibli.product.dto.ProductRequest;
import com.blibli.product.dto.ProductResponse;
import com.blibli.product.enums.CategoryType;
import com.blibli.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;


    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        
        log.info("Creating new product: {}", request.getSku());
        
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        
        log.info("Updating product: {}", id);
        
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }


    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable String id) {
        log.debug("Fetching product: {}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySku(@PathVariable String sku){
        log.debug("Fetching product: {}", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Fetching all products: page={}, size={}", page, size);
        PageResponse<ProductResponse> response = productService.getAllProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable CategoryType category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Fetching products by category: {}, page={}, size={}", category, page, size);
        PageResponse<ProductResponse> response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        
        log.info("Deleting product: {}", id);
        
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
