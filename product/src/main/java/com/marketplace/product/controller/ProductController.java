package com.marketplace.product.controller;

import com.marketplace.common.command.Command;
import com.marketplace.common.command.CommandInvoker;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.product.command.SeedProductsCommand;
import com.marketplace.product.dto.response.ProductResponse;
import com.marketplace.product.mapper.ProductMapper;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for product operations
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CommandInvoker commandInvoker;

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedProducts() {
        log.info("Seed products request received");

        Command<Void> command = new SeedProductsCommand(productService);
        commandInvoker.executeCommand(command);

        return ResponseEntity.ok(ApiResponse.success("Products seeded successfully", "Database populated"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Search products request - name: '{}', page: {}, size: {}", name, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchProducts(name, pageable)
                .map(ProductMapper::toProductResponse);

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        log.info("Get product request for ID: {}", id);

        ProductResponse product = ProductMapper.toProductResponse(
                productService.getProductById(id));

        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
