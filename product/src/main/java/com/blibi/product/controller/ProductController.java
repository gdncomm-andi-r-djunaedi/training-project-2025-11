package com.blibi.product.controller;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.service.ProductService;
import com.blibi.product.wrapper.GenericResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {


    private final ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // End Point to Create Product for Testing Purpose.

    @PostMapping("/createProduct")
    public ResponseEntity<GenericResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        // Delegate to service layer for business logic
        ProductDTO savedProduct = productService.createProduct(productDTO);
        // Wrap response in GenericResponse for consistent API structure
        return new ResponseEntity<>(GenericResponse.success(savedProduct, "Product created successfully"),
                HttpStatus.CREATED);
    }


    // This endpoint performs a case-sensitive exact match search for products
     // with the specified name. Results are paginated for efficient data retrieval.

    @GetMapping("/productDetail/productName/{productName}")
    public ResponseEntity<GenericResponse<Page<ProductDTO>>> getProductByName(
            @PathVariable String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        // Create Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        // Fetch products from service layer
        Page<ProductDTO> products = productService.viewProductDetailsByName(productName, pageable);
        // Return paginated results
        return new ResponseEntity<>(GenericResponse.success(products, "Product details fetched successfully"),
                HttpStatus.OK);
    }

    /**
     * Searches products by name using case-insensitive partial matching.
     * This endpoint performs a wildcard search using MongoDB regex, allowing
     * partial matches. The search is case-insensitive, so "laptop" will match
     * "Laptop", "LAPTOP", etc.
     */

    @PostMapping("/searchProduct/productName/{productName}")
    public ResponseEntity<GenericResponse<Page<ProductDTO>>> searchProductByName(
            @PathVariable String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        // Create Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        // Perform case-insensitive search via service layer
        Page<ProductDTO> products = productService.searchProductByName(productName, pageable);
        // Return search results
        return new ResponseEntity<>(GenericResponse.success(products, "Products found successfully"), HttpStatus.OK);
    }

    /**
     * Searches products by category using case-insensitive partial matching.
     * 
     * This endpoint retrieves all products belonging to a specific category.
     * The search uses MongoDB regex for case-insensitive matching, allowing
     * partial category name matches.
     */

    @GetMapping("/searchProduct/category/{category}")
    public ResponseEntity<GenericResponse<Page<ProductDTO>>> searchProductByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        // Create Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        // Fetch products by category from service layer
        Page<ProductDTO> products = productService.searchProductByCategory(category, pageable);
        // Return category-based results
        return new ResponseEntity<>(GenericResponse.success(products, "Products found by category successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieves a single product by its business product identifier.
     * This endpoint fetches detailed information about a specific product
     * using its business productId (e.g., MTA-000001)
     */

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<ProductDTO>> details(@PathVariable String id) {
        // Log the request for debugging and monitoring
        log.info("Fetching details for product ID: {}", id);
        // Fetch product from service layer (throws exception if not found)
        ProductDTO product = productService.getProductDetail(id);
        // Return product details
        return new ResponseEntity<>(
                GenericResponse.success(product, "Product details fetched successfully"),
                HttpStatus.OK);
    }

}