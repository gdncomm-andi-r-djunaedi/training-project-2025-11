package com.blibli.SearchService.repository;


import com.blibli.SearchService.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByProductNameContainingIgnoreCase(
            String productName,
            Pageable pageable
    );

    ProductDocument findBySku(String sku);

}
