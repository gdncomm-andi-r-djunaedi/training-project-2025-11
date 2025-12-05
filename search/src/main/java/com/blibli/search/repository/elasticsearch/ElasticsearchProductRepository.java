package com.blibli.search.repository.elasticsearch;

import com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchProductRepository extends ElasticsearchRepository<ElasticsearchProductDocument, String> {
    
    Page<ElasticsearchProductDocument> findByNameContaining(String name, Pageable pageable);
    
    Page<ElasticsearchProductDocument> findByDescriptionContaining(String description, Pageable pageable);
    
    Page<ElasticsearchProductDocument> findByCategory(String category, Pageable pageable);
}

