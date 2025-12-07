package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public Page<Product> getProducts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Convert 1-based page to 0-based for Spring Data
        return service.getProducts(Math.max(0, page - 1), size);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return service.getProduct(id);
    }

    @GetMapping("/search")
    public Page<Product> searchProducts(@RequestParam String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Convert 1-based page to 0-based for Spring Data
        return service.searchProducts(name, Math.max(0, page - 1), size);
    }

}
