package com.example.product.controller;

import com.example.commandlib.CommandExecutor;
import com.example.product.command.*;
import com.example.product.entity.Product;
import com.example.product.repo.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository repo;
    private final CommandExecutor commandExecutor = new CommandExecutor();

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @PreDestroy
    public void shutdown() {
        commandExecutor.shutdown();
    }

    @GetMapping
    public Page<Product> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return commandExecutor.execute(new ListProductsCommand(repo, page, size));
    }

    @GetMapping("/search")
    public Page<Product> search(@RequestParam String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return commandExecutor.execute(new SearchProductsCommand(repo, q, page, size));
    }

    @GetMapping("/{id}")
    public Product detail(@PathVariable String id) {
        return commandExecutor.execute(new GetProductDetailCommand(repo, id));
    }

    @PostConstruct
    public void init() {
        try {
            if (repo.count() == 0) {
                IntStream.range(1, 51).forEach(i -> repo.save(Product.builder()
                        .sku(String.format("MTA%04d", i))
                        .name("Sample Product Alfred" + i)
                        .description("Test Product Alfred " + i)
                        .price(9.99 + i)
                        .build()));
            }
        } catch (Exception e) {
            // Log warning but don't fail startup - DB might not be ready yet
            System.err.println("Warning: Could not initialize products - " + e.getMessage());
        }
    }
}
