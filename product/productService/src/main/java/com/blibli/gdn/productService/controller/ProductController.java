package com.blibli.gdn.productService.controller;

import com.blibli.gdn.productService.dto.GdnResponseData;
import com.blibli.gdn.productService.dto.request.ProductRequest;
import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.service.ProductSearchService;
import com.blibli.gdn.productService.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    
    @Autowired(required = false)
    private ProductSearchService productSearchService;

    @PostMapping
    public ResponseEntity<GdnResponseData<ProductResponse>> createProduct(
            @jakarta.validation.Valid @RequestBody ProductRequest productRequest,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("Create product request received for: {}", productRequest.getName());

        if (role == null || !role.contains("ROLE_ADMIN")) {
            log.warn("Forbidden: User does not have ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductResponse created = productService.createProduct(productRequest);
        GdnResponseData<ProductResponse> response = GdnResponseData.<ProductResponse>builder()
                .data(created)
                .message("Product created successfully")
                .status(HttpStatus.CREATED.value())
                .success(true)
                .build();

        log.info("Product created successfully with ID: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GdnResponseData<ProductResponse>> getProduct(@PathVariable String id) {
        log.info("Get product request for ID: {}", id);
        ProductResponse product = productService.getProduct(id);

        GdnResponseData<ProductResponse> response = GdnResponseData.<ProductResponse>builder()
                .data(product)
                .message("Product retrieved successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GdnResponseData<ProductResponse>> updateProduct(
            @PathVariable String id,
            @jakarta.validation.Valid @RequestBody ProductRequest productRequest,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("Update product request for ID: {}", id);

        if (role == null || !role.contains("ROLE_ADMIN")) {
            log.warn("Forbidden: User does not have ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProductResponse updated = productService.updateProduct(id, productRequest);
        GdnResponseData<ProductResponse> response = GdnResponseData.<ProductResponse>builder()
                .data(updated)
                .message("Product updated successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GdnResponseData<Void>> deleteProduct(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        log.info("Delete product request for ID: {}", id);

        if (role == null || !role.contains("ROLE_ADMIN")) {
            log.warn("Forbidden: User does not have ROLE_ADMIN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.deleteProduct(id);
        GdnResponseData<Void> response = GdnResponseData.<Void>builder()
                .data(null)
                .message("Product deleted successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GdnResponseData<Page<ProductResponse>>> searchProducts(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        log.info("Search products request - name: {}, category: {}, page: {}, size: {}", name, category, page, size);

        // Check if search contains wildcards or if Elasticsearch is available
        boolean hasWildcard = (name != null && (name.contains("*") || name.contains("?")));
        boolean useElasticsearch = productSearchService != null && (hasWildcard || true); // Use ES if available
        
        Page<ProductResponse> products;
        
        if (useElasticsearch) {
            // Use Elasticsearch search (supports wildcards and better performance)
            log.info("Using Elasticsearch search for query: {}", name);
            Pageable pageable = PageRequest.of(page, size);
            products = productSearchService.searchProducts(
                    name != null ? name : "", 
                    category, 
                    pageable, 
                    sort);
        } else {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            products = productService.searchProducts(name, category, pageable);
        }

        GdnResponseData<Page<ProductResponse>> response = GdnResponseData.<Page<ProductResponse>>builder()
                .data(products)
                .message("Products retrieved successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<GdnResponseData<Page<ProductResponse>>> searchProductsPost(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "price,asc") String sort) {

        log.info("POST search products request - name: {}, category: {}, page: {}, size: {}, sort: {}", 
                name, category, page, size, sort);

        if (productSearchService == null) {
            log.warn("Elasticsearch search service is not available. Returning empty results.");
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductResponse> emptyPage = Page.empty(pageable);
            GdnResponseData<Page<ProductResponse>> response = GdnResponseData.<Page<ProductResponse>>builder()
                    .data(emptyPage)
                    .message("Elasticsearch is not available. Please enable it in application.properties")
                    .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productSearchService.searchProducts(
                name != null ? name : "", 
                category, 
                pageable, 
                sort);

        GdnResponseData<Page<ProductResponse>> response = GdnResponseData.<Page<ProductResponse>>builder()
                .data(products)
                .message("Products retrieved successfully")
                .status(HttpStatus.OK.value())
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }
}
