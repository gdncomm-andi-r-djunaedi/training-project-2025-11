package com.blibli.search.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.blibli.search.entity.elasticsearch.ElasticsearchProductDocument;
import com.blibli.search.repository.elasticsearch.ElasticsearchProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchSearchService {

    private final ElasticsearchProductRepository elasticsearchProductRepository;
    private final ElasticsearchClient elasticsearchClient;

    public void indexProduct(Map<String, Object> productData) {
        String productId = (String) productData.get("id");
        log.info("========== Indexing product in Elasticsearch ==========");
        log.info("Product ID: {}", productId);
        log.info("Product data received: {}", productData);
        
        try {
            ElasticsearchProductDocument doc = ElasticsearchProductDocument.builder()
                    .id(productId)
                    .name((String) productData.get("name"))
                    .description((String) productData.get("description"))
                    .price(extractPrice(productData.get("price")))
                    .category(extractCategory(productData.get("category")))
                    .build();

            log.info("Document to index: id={}, name={}, price={}, category={}", 
                    doc.getId(), doc.getName(), doc.getPrice(), doc.getCategory());

            elasticsearchProductRepository.save(doc);
            log.info("Product indexed successfully in Elasticsearch: {}", doc.getId());
            
            // Verify the document was saved
            boolean exists = elasticsearchProductRepository.existsById(productId);
            log.info("Document exists check: {}", exists);
        } catch (Exception e) {
            log.error("Failed to index product {}: {}", productId, e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getName());
            e.printStackTrace();
            throw new com.blibli.search.exception.ElasticsearchException("Failed to index product in Elasticsearch: " + e.getMessage(), e);
        }
    }

    public void deleteProduct(String id) {
        log.info("Deleting product from Elasticsearch: {}", id);
        try {
            elasticsearchProductRepository.deleteById(id);
            log.info("Product deleted successfully from Elasticsearch: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete product {}: {}", id, e.getMessage(), e);
            throw new com.blibli.search.exception.ElasticsearchException("Failed to delete product from Elasticsearch: " + e.getMessage(), e);
        }
    }

    public Page<ElasticsearchProductDocument> searchByName(String name, Pageable pageable) {
        return elasticsearchProductRepository.findByNameContaining(name, pageable);
    }

    public List<Map<String, Object>> wildcardSearch(String query, int page, int size) {
        log.info("Wildcard search: query={}, page={}, size={}", query, page, size);
        try {
            String searchTerm = query.trim();
            
            log.info("Search term: {}", searchTerm);

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(q -> q
                            .queryString(qs -> qs
                                    .query("*" + searchTerm + "*")
                                    .fields("name")))
                    .from(page * size)
                    .size(size));

            log.info("Executing wildcard search request");
            @SuppressWarnings({"unchecked", "rawtypes"})
            SearchResponse<Map> searchResponse = 
                    elasticsearchClient.search(searchRequest, Map.class);
            
            long totalHits = 0;
            if (searchResponse.hits().total() != null) {
                totalHits = searchResponse.hits().total().value();
            }
            log.info("Search response received: {} hits", totalHits);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<Map> hit : searchResponse.hits().hits()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doc = (Map<String, Object>) hit.source();
                if (doc != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", doc.get("id"));
                    result.put("name", doc.get("name"));
                    result.put("description", doc.get("description"));
                    result.put("price", doc.get("price"));
                    result.put("category", doc.get("category"));
                    results.add(result);
                }
            }
            
            log.info("Wildcard search completed: {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Wildcard search failed: query={}", query, e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new com.blibli.search.exception.SearchException("Wildcard search failed: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> advancedSearch(String query, int page, int size) {
        log.info("Advanced search: query={}, page={}, size={}", query, page, size);
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("products")
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("name^2", "description", "category")
                                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)))
                    .from(page * size)
                    .size(size));

            log.info("Executing advanced search request");
            @SuppressWarnings({"unchecked", "rawtypes"})
            SearchResponse<Map> searchResponse = 
                    elasticsearchClient.search(searchRequest, Map.class);
            
            long totalHits = 0;
            if (searchResponse.hits().total() != null) {
                totalHits = searchResponse.hits().total().value();
            }
            log.info("Search response received: {} hits", totalHits);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<Map> hit : searchResponse.hits().hits()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> doc = (Map<String, Object>) hit.source();
                if (doc != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", doc.get("id"));
                    result.put("name", doc.get("name"));
                    result.put("description", doc.get("description"));
                    result.put("price", doc.get("price"));
                    result.put("category", doc.get("category"));
                    results.add(result);
                }
            }
            
            log.info("Advanced search completed: {} results", results.size());
            return results;
        } catch (Exception e) {
            log.error("Advanced search failed: query={}", query, e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new com.blibli.search.exception.SearchException("Advanced search failed: " + e.getMessage(), e);
        }
    }

    private Double extractPrice(Object priceObj) {
        if (priceObj == null) {
            log.warn("Price is null");
            return null;
        }
        
        if (priceObj instanceof Number) {
            return ((Number) priceObj).doubleValue();
        }

        if (priceObj instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) priceObj).doubleValue();
        }

        try {
            return Double.parseDouble(priceObj.toString());
        } catch (NumberFormatException e) {
            log.warn("Could not parse price: {}", priceObj);
            return null;
        }
    }

    private String extractCategory(Object categoryObj) {
        if (categoryObj == null) {
            log.warn("Category is null");
            return null;
        }
        
        // handle enum of category
        if (categoryObj instanceof Enum) {
            return ((Enum<?>) categoryObj).name();
        }


        // handle map if category is nested object
        if (categoryObj instanceof Map) {
            Map<?, ?> categoryMap = (Map<?, ?>) categoryObj;
            Object name = categoryMap.get("name");
            if (name != null) {
                return name.toString();
            }
            return categoryMap.toString();
        }
        
        return categoryObj.toString();
    }
}

