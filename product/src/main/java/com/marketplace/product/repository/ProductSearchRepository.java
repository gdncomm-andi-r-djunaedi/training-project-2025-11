package com.marketplace.product.repository;

import com.marketplace.product.document.ProductSearchDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDoc, String> {

    Page<ProductSearchDoc> findByNameContaining(String name, Pageable pageable);

    Page<ProductSearchDoc> findByNameOrDescriptionContaining(String name, String description, Pageable pageable);
}
