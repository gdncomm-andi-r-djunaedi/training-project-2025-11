package com.training.productService.productmongo.repository;

import com.training.productService.productmongo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductCustomRepository {
    Page<Product> searchProducts(String searchTerm, Pageable pageable);
}
