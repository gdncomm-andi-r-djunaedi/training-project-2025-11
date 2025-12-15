package com.example.product.command;

import com.example.commandlib.Command;
import com.example.product.entity.Product;
import com.example.product.repo.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class SearchProductsCommand implements Command<Page<Product>> {
    private final ProductRepository repo;
    private final String query;
    private final int page;
    private final int size;

    public SearchProductsCommand(ProductRepository repo, String query, int page, int size) {
        this.repo = repo;
        this.query = query;
        this.page = page;
        this.size = size;
    }

    @Override
    public Page<Product> execute() {
        return repo.search(query, PageRequest.of(page, size));
    }
}

