package com.marketplace.search.repository;

import com.marketplace.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

    @Query("{\"bool\": {\"should\": [" +
            "{\"match\": {\"title\": {\"query\": \"?0\", \"boost\": 2}}}, " +
            "{\"match\": {\"description\": {\"query\": \"?0\"}}}" +
            "], \"minimum_should_match\": 1}}")
    Page<ProductDocument> searchByTitleOrDescription(String query, Pageable pageable);
}
