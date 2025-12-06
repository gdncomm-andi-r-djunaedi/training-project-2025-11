package com.gdn.training.product.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.specification.ProductSpecification;

/**
 * Product repository interface
 */
public interface ProductRepository {
    Optional<Product> findById(UUID id);

    List<Product> search(ProductSpecification spec, int page, int size, String sort);

    long count(ProductSpecification spec);
}
