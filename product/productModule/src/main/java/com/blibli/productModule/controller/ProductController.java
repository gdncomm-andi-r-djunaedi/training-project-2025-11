package com.blibli.productModule.controller;

import com.blibli.productModule.dto.ApiResponse;
import com.blibli.productModule.dto.ProductDto;
import com.blibli.productModule.dto.ProductSearchResponseDto;
import com.blibli.productModule.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products, search, and product details")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Create a new product", description = "Create a new product in the product service")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@RequestBody ProductDto productDto) {
        log.info("POST /api/products - productId: {}", productDto.getProductId());
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(ApiResponse.success(createdProduct), HttpStatus.CREATED);
    }

    @Operation(summary = "Search products", description = "Search products with pagination and " + "optional category filter")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductSearchResponseDto>> searchProducts(@RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String category, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/products/search - searchTerm: {}, category: {}, page: {}, size: {}",
                searchTerm, category, page, size);
        ProductSearchResponseDto productSearchResponseDto =
                productService.searchProducts(searchTerm, category, page, size);
        return ResponseEntity.ok(ApiResponse.success(productSearchResponseDto));
    }

    @Operation(summary = "Get all products", description = "Get paginated list of all active products with optional category filter")
    @GetMapping("/listing")
    public ResponseEntity<ApiResponse<ProductSearchResponseDto>> getAllProducts(
            @RequestParam(required = false) String category, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/products - category: {}, page: {}, size: {}", category, page, size);
        ProductSearchResponseDto productSearchResponseDto = productService.getAllProducts(category, page, size);
        return ResponseEntity.ok(ApiResponse.success(productSearchResponseDto));
    }

    @Operation(summary = "Get product by ID", description = "Get detailed information about a specific product using its productId")
    @GetMapping("/detail/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable String productId) {
        log.info("GET /api/products/{}", productId);
        ProductDto product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @Operation(summary = "Delete a product", description = "Delete a product by productId and evict from cache")
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable String productId) {
        log.info("DELETE /api/products/{}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }

}
