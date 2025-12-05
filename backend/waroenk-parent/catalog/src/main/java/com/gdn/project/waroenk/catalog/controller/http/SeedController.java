package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.service.ProductSeedDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for seeding test data.
 * Used for development and testing purposes.
 * All seed operations are async - they return immediately and process in background.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/seed")
@RequiredArgsConstructor
@Tag(name = "Seed Data", description = "Endpoints for seeding test data (async)")
public class SeedController {

  private final ProductSeedDataGenerator productSeedDataGenerator;
  private final MongoTemplate mongoTemplate;

  @PostMapping("/products/{merchantCode}")
  @Operation(summary = "Generate products for a specific merchant (async)")
  public ResponseEntity<Map<String, Object>> seedProductsForMerchant(
      @PathVariable String merchantCode,
      @RequestParam(defaultValue = "100") int count
  ) {
    log.info("Starting async seed of {} products for merchant: {}", count, merchantCode);
    
    // Start async process
    productSeedDataGenerator.generateProductsForMerchantAsync(merchantCode, count);
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "STARTED");
    response.put("merchantCode", merchantCode);
    response.put("requestedCount", count);
    response.put("message", "Seed process started. Check logs for progress.");
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @PostMapping("/products/all")
  @Operation(summary = "Generate products for all merchants (async)")
  public ResponseEntity<Map<String, Object>> seedProductsForAllMerchants(
      @RequestParam(defaultValue = "100") int productsPerMerchant,
      @RequestParam(defaultValue = "1") int batchStart,
      @RequestParam(defaultValue = "100") int batchEnd
  ) {
    log.info("Starting async seed of {} products per merchant for merchants {} to {}", 
        productsPerMerchant, batchStart, batchEnd);
    
    // Start async process
    productSeedDataGenerator.generateProductsForAllMerchantsAsync(productsPerMerchant, batchStart, batchEnd);
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "STARTED");
    response.put("productsPerMerchant", productsPerMerchant);
    response.put("batchStart", batchStart);
    response.put("batchEnd", batchEnd);
    response.put("message", "Seed process started. Check logs for progress or call /status endpoint.");
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @GetMapping("/status")
  @Operation(summary = "Get current seed data status")
  public ResponseEntity<Map<String, Object>> getSeedStatus() {
    long brandCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "brands");
    long categoryCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "categories");
    long merchantCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "merchants");
    long productCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "products");
    long variantCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "variants");
    long inventoryCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "inventories");
    
    Map<String, Object> response = new HashMap<>();
    response.put("brands", brandCount);
    response.put("categories", categoryCount);
    response.put("merchants", merchantCount);
    response.put("products", productCount);
    response.put("variants", variantCount);
    response.put("inventories", inventoryCount);
    
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/products")
  @Operation(summary = "Delete all products, variants, and inventories (use with caution)")
  public ResponseEntity<Map<String, Object>> deleteAllProducts() {
    log.warn("Deleting all products, variants, and inventories");
    
    mongoTemplate.dropCollection("products");
    mongoTemplate.dropCollection("variants");
    mongoTemplate.dropCollection("inventories");
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "All products, variants, and inventories deleted");
    
    return ResponseEntity.ok(response);
  }

  // ==================== TypeSense Indexing Endpoints ====================

  @PostMapping("/index/merchants")
  @Operation(summary = "Index all merchants in TypeSense (async)")
  public ResponseEntity<Map<String, Object>> indexMerchantsInTypeSense() {
    log.info("Starting async TypeSense indexing for merchants");
    
    productSeedDataGenerator.indexAllMerchantsInTypeSenseAsync();
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "STARTED");
    response.put("message", "TypeSense merchant indexing started. Check logs for progress.");
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @PostMapping("/index/products")
  @Operation(summary = "Index all products in TypeSense (async)")
  public ResponseEntity<Map<String, Object>> indexProductsInTypeSense() {
    log.info("Starting async TypeSense indexing for products");
    
    productSeedDataGenerator.indexAllProductsInTypeSenseAsync();
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "STARTED");
    response.put("message", "TypeSense product indexing started. Check logs for progress.");
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @PostMapping("/index/all")
  @Operation(summary = "Index all merchants and products in TypeSense (async)")
  public ResponseEntity<Map<String, Object>> indexAllInTypeSense() {
    log.info("Starting async TypeSense indexing for all data");
    
    // Index merchants first, then products
    productSeedDataGenerator.indexAllMerchantsInTypeSenseAsync();
    productSeedDataGenerator.indexAllProductsInTypeSenseAsync();
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "STARTED");
    response.put("message", "TypeSense indexing started for merchants and products. Check logs for progress.");
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
