package com.gdn.training.product.controller;

import com.gdn.training.common.model.BaseResponse;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog and search endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Search products", description = "Search and list products with pagination")
    public ResponseEntity<BaseResponse<Page<Product>>> searchProducts(
            @RequestParam(required = false) String query,
            Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.success(productService.searchProducts(query, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Get detailed information about a specific product")
    public ResponseEntity<BaseResponse<Product>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(BaseResponse.success(productService.getProductById(id)));
    }
}
