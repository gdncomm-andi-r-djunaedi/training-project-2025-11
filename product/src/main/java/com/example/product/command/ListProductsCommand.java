package com.example.product.command;

import com.example.commandlib.Command;
import com.example.product.entity.Product;
import com.example.product.repo.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ListProductsCommand implements Command<Page<Product>> {
    private final ProductRepository repo;
    private final int page;
    private final int size;

    public ListProductsCommand(ProductRepository repo, int page, int size) {
        this.repo = repo;
        this.page = page;
        this.size = size;
    }

    @Override
    public Page<Product> execute() {
        Pageable p = PageRequest.of(page, size);
        return repo.findAll(p);
    }
}

