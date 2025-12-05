package com.example.product.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.example.product.repository.ProductRepository;
import com.example.product.model.Product;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<Product>> list(@RequestParam(value="q", required=false) String q) {
        if (q == null || q.isEmpty()) {
            return ResponseEntity.ok(productRepository.findAll());
        }
        return ResponseEntity.ok(productRepository.findByNameContainingIgnoreCase(q));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        return ResponseEntity.ok(productRepository.save(p));
    }
}
