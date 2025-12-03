package com.gdn.marketplace.product.controller;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return service.saveProduct(product);
    }

    @GetMapping
    public Page<Product> findAllProducts(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return service.getProducts(page, size);
    }

    @GetMapping("/{id}")
    public Product findProductById(@PathVariable String id) {
        return service.getProductById(id);
    }

    @GetMapping("/search")
    public Page<Product> searchProducts(@RequestParam String name,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return service.searchProducts(name, page, size);
    }

    @PutMapping
    public Product updateProduct(@RequestBody Product product) {
        return service.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable String id) {
        return service.deleteProduct(id);
    }
}
