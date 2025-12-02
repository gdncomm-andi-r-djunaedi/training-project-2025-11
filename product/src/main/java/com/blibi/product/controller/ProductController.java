package com.blibi.product.controller;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.service.ProductService;
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

    @PostMapping("/createProduct")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO savedProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/productDetail/productName/{productName}")
    public ResponseEntity<Page<ProductDTO>> getProductByName(
            @PathVariable String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.viewProductDetailsByName(productName, pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping("/searchProduct/productName/{productName}")
    public ResponseEntity<Page<ProductDTO>> searchProductByName(
            @PathVariable String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProductByName(productName, pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/searchProduct/category/{category}")
    public ResponseEntity<Page<ProductDTO>> searchProductByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProductByCategory(category, pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
}