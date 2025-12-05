package com.kailash.product.service.impl;

import com.kailash.product.entity.Product;
import com.kailash.product.repository.ProductRepository;
import com.kailash.product.service.ProductEventProducer;
import com.kailash.product.service.ProductIndexService;
import com.kailash.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository repo;
    @Autowired
    ProductIndexService productIndexService;
    @Autowired
    ProductEventProducer productEventProducer;

    @Override
    public Product create(Product p) {
        repo.findBySku(p.getSku()).ifPresent(existing -> { throw new IllegalArgumentException("SKU exists"); });

     Product saved=repo.save(p);
     productEventProducer.sendProductUpsert(saved);
//        productIndexService.index(saved);
        return saved;
    }

    @Override
    @Cacheable(value = "product", key = "#root.args[0]")
    public Optional<Product> findBySku(String sku) {
        return repo.findBySku(sku);
    }

    @Override
    @Cacheable(value = "product", key = "#root.args[0]")
    public Page<Product> list(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (search == null || search.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByNameContainingIgnoreCase(search, pageable);
    }

    @Override
    @CachePut(value = "product", key = "#root.args[0]")
    public Product update(String sku, Product updated) {

        return repo.findBySku(sku).map(existing -> {
            if (updated.getName() != null) existing.setName(updated.getName());
            if (updated.getShortDescription() != null) existing.setShortDescription(updated.getShortDescription());
            if (updated.getPrice() != null) existing.setPrice(updated.getPrice());
            Product productToProduce=repo.save(existing);
            productEventProducer.sendProductUpsert(productToProduce);
            return productToProduce;
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @Override
    @CacheEvict(value = "product", key = "#root.args[0]")
    public void delete(String sku) {

        repo.findBySku(sku).ifPresent(p -> {
            repo.deleteById(p.getId());
            productEventProducer.sendProductDelete(p.getId());
        });


    }
}
