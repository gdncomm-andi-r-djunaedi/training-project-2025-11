package com.Product.ProductService.controller;

import com.Product.ProductService.dto.ProductResponseDTO;
import com.Product.ProductService.exceptions.ProductServiceExceptions;
import com.Product.ProductService.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProductList(Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProducts(pageable);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchAndFilterProducts(@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/getProductById/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetail(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ProductServiceExceptions("Product ID is invalid", HttpStatus.BAD_REQUEST);
        }

        ProductResponseDTO productDto = productService.getProductById(id);
        return ResponseEntity.ok(productDto);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductResponseDTO productResponseDTO) {
        return ResponseEntity.status(201).body(productService.saveProduct(productResponseDTO));
    }

}
