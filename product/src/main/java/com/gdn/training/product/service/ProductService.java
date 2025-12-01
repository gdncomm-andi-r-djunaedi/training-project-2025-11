package com.gdn.training.product.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            log.debug("Listing all products - page {}", pageable.getPageNumber());
            return productRepository.findAll(pageable);
        }

        String trimmed = query.trim();
        if (containsWildcard(trimmed)) {
            String pattern = toLikePattern(trimmed);
            log.info("Searching products with wildcard pattern {}", pattern);
            return productRepository.findByNameLikeIgnoreCase(pattern, pageable);
        }

        log.info("Searching products with keyword '{}'", trimmed);
        return productRepository.findByNameContainingIgnoreCase(trimmed, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .map(product -> {
                    log.debug("Product {} found", id);
                    return product;
                })
                .orElseThrow(() -> {
                    log.warn("Product {} not found", id);
                    return new IllegalArgumentException("Product not found");
                });
    }

    private boolean containsWildcard(String query) {
        return query.contains("*") || query.contains("?");
    }

    private String toLikePattern(String query) {
        String pattern = query
                .replace('*', '%')
                .replace('?', '_');
        if (!pattern.contains("%") && !pattern.contains("_")) {
            pattern = "%" + pattern + "%";
        }
        return pattern;
    }
}
