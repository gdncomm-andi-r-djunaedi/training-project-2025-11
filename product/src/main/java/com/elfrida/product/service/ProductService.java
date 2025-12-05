package com.elfrida.product.service;

import com.elfrida.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Product createProduct(Product product);

    Page<Product> getAllProducts(Pageable pageable);

    Page<Product> searchProducts(String name, Pageable pageable);

    Product getProductById(String id);
}
