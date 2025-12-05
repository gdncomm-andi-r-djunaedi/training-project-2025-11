package com.gdn.training.product.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gdn.training.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByNameLikeIgnoreCase(String pattern, Pageable pageable);
}
