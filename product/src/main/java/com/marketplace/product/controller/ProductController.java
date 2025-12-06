package com.marketplace.product.controller;

import com.marketplace.common.controller.BaseCommandController;
import com.marketplace.common.dto.ApiResponse;
import com.marketplace.common.mapper.MapperService;
import com.marketplace.product.command.GetProductByIdCommand;
import com.marketplace.product.command.SearchProductsCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.dto.request.SearchProductsRequest;
import com.marketplace.product.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for product operations.
 * Provides endpoints for searching and retrieving product details.
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
@Tag(name = "Product", description = "Product catalog operations")
public class ProductController extends BaseCommandController {

    private static final int MAX_PAGE_SIZE = 100;

    private final MapperService mapperService;

    @Autowired
    public ProductController(MapperService mapperService) {
        this.mapperService = mapperService;
    }

    /**
     * Search products by name with pagination.
     *
     * @param name Search term for product name (case-insensitive, partial match)
     * @param page Page number (0-indexed)
     * @param size Page size (max 100)
     * @return Paginated list of matching products
     */
    @Operation(summary = "Search products", description = "Search products by name with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @Parameter(description = "Search term for product name") @RequestParam(required = false, defaultValue = "") String name,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "10") int size) {

        // Validate and cap page size to prevent excessive memory usage
        int validatedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int validatedPage = Math.max(page, 0);

        if (size > MAX_PAGE_SIZE) {
            log.warn("Requested page size {} exceeds maximum, capping to {}", size, MAX_PAGE_SIZE);
        }

        log.info("Search products request - name: '{}', page: {}, size: {}", name, validatedPage, validatedSize);

        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        SearchProductsRequest request = SearchProductsRequest.builder()
                .name(name)
                .pageable(pageable)
                .build();

        Page<ProductResponse> response = execute(SearchProductsCommand.class, request);
        return okResponse(response);
    }

    /**
     * Get product by ID.
     *
     * @param id Product ID
     * @return Product details
     */
    @Operation(summary = "Get product by ID", description = "Retrieve product details by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "Product ID") @PathVariable String id) {
        log.info("Get product request for ID: {}", id);

        GetProductByIdRequest request = GetProductByIdRequest.builder().productId(id).build();
        ProductResponse response = execute(GetProductByIdCommand.class, request);
        return okResponse(response);
    }
}
