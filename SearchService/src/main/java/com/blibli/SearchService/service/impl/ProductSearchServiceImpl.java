package com.blibli.SearchService.service.impl;


import com.blibli.SearchService.entity.ProductDocument;
import com.blibli.SearchService.exception.InvalidSearchException;
import com.blibli.SearchService.exception.ProductNotFoundException;
import com.blibli.SearchService.repository.ProductSearchRepository;
import com.blibli.SearchService.service.ProductSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchServiceImpl(ProductSearchRepository repository, ElasticsearchOperations elasticsearchOperations) {
        this.repository = repository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Page<ProductDocument> searchByName(
            String name,
            int page,
            int size
    ) {

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidSearchException("Product name must not be empty");
        }

        if (page < 0 || size <= 0) {
            throw new InvalidSearchException("Invalid pagination parameters");
        }

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("productName")
                                .query(name)
                        )
                )
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        if (hits.getTotalHits() == 0) {
            throw new ProductNotFoundException(
                    "No products found with name: " + name
            );
        }

        List<ProductDocument> content = hits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        return new PageImpl<>(
                content,
                PageRequest.of(page, size),
                hits.getTotalHits()
        );
    }


    public ProductDocument searchBySku(
            String sku) {

        if (sku == null || sku.trim().isEmpty()) {
            throw new InvalidSearchException("SKU must not be empty");
        }


        ProductDocument result = repository.findBySku(sku);

        if (result == null) {
            throw new ProductNotFoundException(
                    "No product found for SKU: " + sku
            );
        }

        return result;
    }
}
