package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.dto.merchant.MerchantResponseDto;
import com.gdn.project.waroenk.catalog.dto.product.AggregatedProductDto;
import com.gdn.project.waroenk.catalog.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController("catalogHttpSearchController")
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search API for products and merchants")
public class SearchController {

  private final SearchService searchService;

  @GetMapping("/search")
  @Operation(summary = "Combined search across products and merchants")
  public CompletableFuture<SearchService.CombinedResult> search(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    return searchService.search(query, size, cursor, sortBy, sortOrder);
  }

  @GetMapping("/search/products")
  @Operation(summary = "Search products")
  public ResponseEntity<SearchService.Result<AggregatedProductDto>> searchProducts(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder, @RequestParam(required = false) Boolean buyable) {
    try {
      SearchService.Result<AggregatedProductDto> result =
          searchService.searchProducts(query, size, cursor, sortBy, sortOrder, buyable);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.warn("Error when search products {}", query, e);
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/search/merchants")
  @Operation(summary = "Search merchants")
  public ResponseEntity<SearchService.Result<MerchantResponseDto>> searchMerchants(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    try {
      SearchService.Result<MerchantResponseDto> result =
          searchService.searchMerchants(query, size, cursor, sortBy, sortOrder);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.warn("Error when search merchants {}", query, e);
      throw new RuntimeException(e);
    }
  }
}
