package com.gdn.training.product_service.controller;

import com.gdn.training.product_service.dto.ProductResponse;
import com.gdn.training.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Product Management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Return paginated product list")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description= "Page number (in index)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort by asc or dec")
            @RequestParam(defaultValue = "asc") String direction){

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ProductResponse> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }

    //search product by name
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Return products by name")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Search query")
            @RequestParam String q,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction){

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ProductResponse> products = productService.searchProducts(q, pageable);

        return ResponseEntity.ok(products);
    }

    //Get products by id

    @GetMapping("/{id}")
    @Operation(summary = "Get products by ID", description = "Return single product detail")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID")
            @PathVariable Long id){

        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

}
