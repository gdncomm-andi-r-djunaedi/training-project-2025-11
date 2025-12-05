package com.marketplace.search.repository;

import com.marketplace.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"description\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}")
    Page<ProductDocument> searchByTitleOrDescription(String query, Pageable pageable);
}
