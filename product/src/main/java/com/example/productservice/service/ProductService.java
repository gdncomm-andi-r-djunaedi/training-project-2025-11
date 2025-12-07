package com.example.productservice.service;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Page<Product> getProducts(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Product getProduct(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new com.example.productservice.exception.ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    public Page<Product> searchProducts(String name, int page, int size) {
        return repository.findByNameRegex(name, PageRequest.of(page, size));
    }
}
