package com.blibli.apigateway.controller;

import com.blibli.apigateway.client.ProductClient;
import com.blibli.apigateway.dto.response.PageResponse;
import com.blibli.apigateway.dto.request.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "Product management API")
public class ProductController {
    
    private final ProductClient productClient;
    
    public ProductController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping("/list")
    public ResponseEntity<?> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductDto> products = productClient.listOfProducts(page, size);

        if (products == null) {
            log.error("Product service returned null response - service may be unavailable");
            throw new RuntimeException("Product service is unavailable. Please try again later.");
        }
        
        if (products.getContent() == null) {
            log.error("Product service returned response with null content - invalid response structure");
            throw new RuntimeException("Product service returned invalid response. Service may be misconfigured.");
        }
        
        log.info("Successfully fetched {} products", products.getContent().size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<?> searchProducts(
            @PathVariable String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductDto> products = productClient.searchProducts(searchTerm, page, size);

        if (products == null) {
            log.error("Product service returned null response for searchterm search - service may be unavailable");
            throw new RuntimeException("Product service is unavailable. Please try again later.");
        }
        
        log.info("Successfully searched products, found {} items", products.getContent() != null ? products.getContent().size() : 0);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/productDetail/{productId}")
    @Operation(summary = "Get product details", description = "Get detailed information about a product by product ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<?> getProductDetails(@PathVariable String productId) {
        ProductDto product = productClient.getProductDetails(productId);

        if (product == null) {
            log.error("Product service returned null for productId: {} - service may be unavailable", productId);
            throw new RuntimeException("Product service is unavailable. Please try again later.");
        }
        
        log.info("Product found: productId={}, name={}", productId, product.getName());
        return ResponseEntity.ok(product);
    }

}

