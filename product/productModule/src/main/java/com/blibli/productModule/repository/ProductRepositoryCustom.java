package com.blibli.productModule.repository;

import com.blibli.productModule.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

  Page<Product> searchProducts(String searchQuery, Pageable pageable);

  Page<Product> searchProductsByCategory(String searchQuery, String category, Pageable pageable);
}

