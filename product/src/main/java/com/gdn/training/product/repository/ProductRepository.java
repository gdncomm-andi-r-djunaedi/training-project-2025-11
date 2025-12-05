package com.gdn.training.product.repository;

import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.gdn.training.product.model.entity.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    public Page<Product> findByNameRegex(Pattern regex, Pageable pageable);
}