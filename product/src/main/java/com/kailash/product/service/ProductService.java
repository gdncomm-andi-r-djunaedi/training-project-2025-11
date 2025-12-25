package com.kailash.product.service;

import com.kailash.product.entity.Product;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ProductService {
    Product create(Product p);
    Optional<Product> findBySku(String sku);
    Page<Product> list(String search, int page, int size);
    Product update(String sku, Product updated);
    void delete(String sku);
}
