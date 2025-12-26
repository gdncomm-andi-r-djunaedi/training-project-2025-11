package com.dev.onlineMarketplace.ProductService.controller;

import com.dev.onlineMarketplace.ProductService.dto.GdnResponseData;
import com.dev.onlineMarketplace.ProductService.dto.ProductDTO;
import com.dev.onlineMarketplace.ProductService.dto.ProductSearchResponse;
import com.dev.onlineMarketplace.ProductService.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping("/search")
    public ResponseEntity<GdnResponseData<ProductSearchResponse>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") int page) {

        log.info("Searching products with query: {}, limit: {}, page: {}", query, limit, page);
        ProductSearchResponse response = productService.searchProducts(query, page, limit);
        return ResponseEntity.ok(GdnResponseData.success(response, "Products found"));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<GdnResponseData<ProductDTO>> getProductByIdOrSku(@PathVariable String identifier) {
        log.info("Fetching product with identifier (ID or SKU): {}", identifier);
        ProductDTO product = productService.getProductByIdOrSku(identifier);
        return ResponseEntity.ok(GdnResponseData.success(product, "Product found"));
    }
}
