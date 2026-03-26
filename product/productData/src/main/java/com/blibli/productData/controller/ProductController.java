package com.blibli.productData.controller;

import com.blibli.productData.dto.ProductDTO;
import com.blibli.productData.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "List all products", description = "Get all products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getAllProductList(pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/search/{searchTerm}")
    @Operation(summary = "Search product", description = "Get products matching name,desc and brand")
    public ResponseEntity<?> searchProduct(
            @PathVariable String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.queryProducts(searchTerm, pageable);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No products found matching: " + searchTerm);
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/view/{productId}")
    @Operation(summary = "View product", description = "View product detail by id")
    public ResponseEntity<?> viewProduct(@PathVariable String productId) {
        ProductDTO productDetail = productService.getProductDetail(productId);
        if (productDetail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found with ID: " + productId);
        }
        return new ResponseEntity<>(productDetail, HttpStatus.OK);
    }
}
