package com.product.service;

import com.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    public Product create(Product product);

    public Product getById(UUID id);

    public List<Product> getAll();

    public Product update(UUID id, Product updated);

    public void delete(UUID id);

    public Page<Product> searchProducts(String keyword, int page, int size);
}
