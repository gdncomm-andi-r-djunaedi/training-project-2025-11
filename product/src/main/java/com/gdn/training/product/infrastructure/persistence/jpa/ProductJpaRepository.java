package com.gdn.training.product.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.training.product.infrastructure.persistence.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    // This method is actually redundant because JpaRepository already gives you:
    // Optional<ProductEntity> findById(UUID id);
    // But you can keep it if you want explicit naming
    Optional<ProductEntity> findById(UUID productId);
}