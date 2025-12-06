package com.training.marketplace.product.repository;

import com.training.marketplace.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductId(String productId);
    Page<ProductEntity> findByProductNameLike(String productName, Pageable pageable);
}
