package com.kailash.product.service.impl;

import com.kailash.product.entity.Product;
import com.kailash.product.entity.ProductIndex;
import com.kailash.product.repository.ProductElasticSearchRepository;
import com.kailash.product.repository.ProductRepository;
import com.kailash.product.service.ProductIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductIndexServiceImpl implements ProductIndexService {

    private final ProductElasticSearchRepository searchRepo;
    private final com.kailash.product.repository.ProductRepository productRepo; // only for reindexAll()

    @Override
    public void index(ProductIndex index) {
        searchRepo.save(index);
    }

    @Override
    public Iterable<ProductIndex> search(String keyword) {

        var nameMatches = searchRepo.findByNameContainingIgnoreCase(keyword);
        var descMatches = searchRepo.findByShortDescriptionContainingIgnoreCase(keyword);

        Set<ProductIndex> merged = new HashSet<>();
        merged.addAll(nameMatches);
        merged.addAll(descMatches);

        return merged;
    }

    @Override
    public void reindexAll() {
        Iterable<Product> products = productRepo.findAll();

        products.forEach(p -> {
            ProductIndex doc = ProductIndex.builder()
                    .id(p.getId())
                    .sku(p.getSku())
                    .name(p.getName())
                    .shortDescription(p.getShortDescription())
                    .price(p.getPrice())
                    .build();

            searchRepo.save(doc);
        });
    }
}
