package com.project.product.controller;

import com.project.product.dto.request.CreateProductRequest;
import com.project.product.dto.request.UpdateProductRequest;
import com.project.product.dto.response.PageResponse;
import com.project.product.dto.response.ProductResponse;
import com.project.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Product with SKU already exists")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("REST request to create product: {}", request.getSku());
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details by ID and increments view count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable String id) {
        log.info("REST request to get product by ID: {}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieves product details by SKU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Retrieves product details by URL slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Product slug") @PathVariable String slug) {
        log.info("REST request to get product by slug: {}", slug);
        ProductResponse response = productService.getProductBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("REST request to update product: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Soft deletes a product (sets isActive to false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable String id) {
        log.info("REST request to delete product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves paginated list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("REST request to get all products - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.getAllProducts(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active products", description = "Retrieves paginated list of active products only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active products retrieved successfully")
    })
    public ResponseEntity<PageResponse<ProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("REST request to get active products");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.getActiveProducts(pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products",
            description = "Searches products by name, description, category, or tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @Parameter(description = "Filter active products only")
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("REST request to search products with keyword: '{}'", keyword);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.searchProducts(
                keyword, activeOnly, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category",
            description = "Retrieves paginated list of products in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Product category") @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("REST request to get products by category: {}", category);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ProductResponse> response = productService.getProductsByCategory(
                category, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    @Operation(summary = "Get products by IDs",
            description = "Retrieves multiple products by their IDs (bulk fetch)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponse>> getProductsByIds(
            @Parameter(description = "List of product IDs")
            @RequestBody List<String> ids) {
        log.info("REST request to get products by IDs: {}", ids);
        List<ProductResponse> response = productService.getProductsByIds(ids);
        return ResponseEntity.ok(response);
    }
}
