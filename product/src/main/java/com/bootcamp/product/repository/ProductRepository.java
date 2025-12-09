package com.bootcamp.product.repository;

import com.bootcamp.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  Product findBySku(String sku);
}
