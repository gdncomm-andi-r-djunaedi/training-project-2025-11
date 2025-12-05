package com.example.product.command;

import com.example.commandlib.Command;
import com.example.product.entity.Product;
import com.example.product.repo.ProductRepository;

public class GetProductDetailCommand implements Command<Product> {
    private final ProductRepository repo;
    private final String id;

    public GetProductDetailCommand(ProductRepository repo, String id) {
        this.repo = repo;
        this.id = id;
    }

    @Override
    public Product execute() {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("not found"));
    }
}
