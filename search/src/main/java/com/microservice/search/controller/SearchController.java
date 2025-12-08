package com.microservice.search.controller;

import com.microservice.search.dto.ApiResponse;
import com.microservice.search.dto.ProductResponseDto;
import com.microservice.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductSearchService productSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            log.info("Received search request: query={}, page={}, size={}", query, page, size);

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search query cannot be empty"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponseDto> results = productSearchService.searchProducts(query.trim(), pageable);

            log.info("Search completed: found {} results for query: {}", results.getTotalElements(), query);

            return ResponseEntity.ok(ApiResponse.success(results,
                    "Search completed successfully"));
        } catch (Exception e) {
            log.error("Error performing search with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error performing search: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            log.info("Received category search request: category={}, page={}, size={}", category, page, size);

            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Category cannot be empty"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponseDto> results = productSearchService.searchByCategory(category.trim(), pageable);

            log.info("Category search completed: found {} results for category: {}",
                    results.getTotalElements(), category);

            return ResponseEntity.ok(ApiResponse.success(results,
                    "Category search completed successfully"));
        } catch (Exception e) {
            log.error("Error searching by category: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching by category: " + e.getMessage()));
        }
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> searchByBrand(
            @PathVariable String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            log.info("Received brand search request: brand={}, page={}, size={}", brand, page, size);

            if (brand == null || brand.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Brand cannot be empty"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponseDto> results = productSearchService.searchByBrand(brand.trim(), pageable);

            log.info("Brand search completed: found {} results for brand: {}",
                    results.getTotalElements(), brand);

            return ResponseEntity.ok(ApiResponse.success(results,
                    "Brand search completed successfully"));
        } catch (Exception e) {
            log.error("Error searching by brand: {}", brand, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching by brand: " + e.getMessage()));
        }
    }

    @GetMapping("/comprehensive")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> comprehensiveSearch(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            log.info("Received comprehensive search request: query={}, page={}, size={}", query, page, size);

            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search query cannot be empty"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponseDto> results = productSearchService.comprehensiveSearch(query.trim(), pageable);

            log.info("Comprehensive search completed: found {} results for query: {}",
                    results.getTotalElements(), query);

            return ResponseEntity.ok(ApiResponse.success(results,
                    "Comprehensive search completed successfully"));
        } catch (Exception e) {
            log.error("Error performing comprehensive search with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error performing comprehensive search: " + e.getMessage()));
        }
    }
}