package com.blibli.gdn.productService.service.impl;

import com.blibli.gdn.productService.dto.response.ProductResponse;
import com.blibli.gdn.productService.mapper.ProductMapper;
import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.ProductDocument;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = false)
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String category, Pageable pageable, String sort) {
        log.info("Searching products in Elasticsearch - name: {}, category: {}, page: {}, size: {}", 
                name, category, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Criteria criteria = null;

            if (name != null && !name.trim().isEmpty()) {
                String searchTerm = name.trim();
                boolean isWildcardSearch = searchTerm.contains("*") || searchTerm.contains("?");
                
                if (isWildcardSearch) {
                    // Use wildcard search for patterns like *phone*, iphone*, *pro, "Aerodynamic * Bag"
                    log.info("Using wildcard search for term: {}", searchTerm);
                    
                    // For wildcard patterns with analyzed text fields, split by * and search for all parts
                    // "Aerodynamic * Bag" -> search for "Aerodynamic" AND "Bag"
                    // This works better with tokenized text fields
                    String[] parts = searchTerm.split("\\*");
                    List<String> searchParts = new java.util.ArrayList<>();
                    for (String part : parts) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            searchParts.add(trimmed);
                        }
                    }
                    
                    if (searchParts.size() > 1) {
                        // Multiple parts - search for all parts (AND condition)
                        // "Aerodynamic * Bag" -> must contain both "Aerodynamic" AND "Bag"
                        // Build criteria: (name contains "Aerodynamic" AND name contains "Bag") OR 
                        //                (description contains "Aerodynamic" AND description contains "Bag")
                        Criteria nameCriteria = new Criteria("name").contains(searchParts.get(0)).boost(2.0f);
                        Criteria descCriteria = new Criteria("description").contains(searchParts.get(0));
                        
                        for (int i = 1; i < searchParts.size(); i++) {
                            nameCriteria = nameCriteria.and(new Criteria("name").contains(searchParts.get(i)).boost(2.0f));
                            descCriteria = descCriteria.and(new Criteria("description").contains(searchParts.get(i)));
                        }
                        
                        // Combine name and description with OR
                        criteria = nameCriteria.or(descCriteria);
                    } else if (searchParts.size() == 1) {
                        // Single part with wildcard at start/end - just search for the part
                        // "*phone*" or "phone*" or "*phone" -> search for "phone"
                        Criteria nameCriteria = new Criteria("name").contains(searchParts.get(0)).boost(2.0f);
                        Criteria descCriteria = new Criteria("description").contains(searchParts.get(0));
                        criteria = nameCriteria.or(descCriteria);
                    }
                } else {
                    // Handle multi-word queries (e.g., "Small Lamp")
                    // Split by spaces and search for all words (AND condition)
                    String[] words = searchTerm.split("\\s+");
                    
                    if (words.length > 1) {
                        // Multiple words - search for all words (AND condition)
                        // "Small Lamp" -> must contain both "Small" AND "Lamp"
                        log.info("Multi-word search detected: {} words", words.length);
                        
                        // Build criteria for name: must contain all words
                        Criteria nameCriteria = new Criteria("name").contains(words[0]).boost(2.0f);
                        for (int i = 1; i < words.length; i++) {
                            nameCriteria = nameCriteria.and(new Criteria("name").contains(words[i]).boost(2.0f));
                        }
                        
                        // Build criteria for description: must contain all words
                        Criteria descCriteria = new Criteria("description").contains(words[0]);
                        for (int i = 1; i < words.length; i++) {
                            descCriteria = descCriteria.and(new Criteria("description").contains(words[i]));
                        }
                        
                        // Combine name and description with OR
                        criteria = nameCriteria.or(descCriteria);
                    } else {
                        // Single word - simple contains search
                        Criteria nameCriteria = new Criteria("name").contains(searchTerm).boost(2.0f);
                        Criteria descCriteria = new Criteria("description").contains(searchTerm);
                        criteria = nameCriteria.or(descCriteria);
                    }
                }
            }

            // Filter by category if provided
            if (category != null && !category.trim().isEmpty()) {
                Criteria categoryCriteria = new Criteria("category").is(category);
                if (criteria != null) {
                    criteria = criteria.and(categoryCriteria);
                } else {
                    criteria = categoryCriteria;
                }
            }

            // If no criteria, return all products (match all)
            if (criteria == null) {
                log.info("No search criteria provided, returning all products");
                criteria = new Criteria(); // Empty criteria matches all
            }

            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria)
                    .setPageable(pageable);

            // Handle sorting - be careful with text fields which may not be sortable
            // Skip sorting for now to avoid query errors, or use keyword fields
            // Sorting on analyzed text fields requires .keyword subfield
            try {
                if (sort != null && !sort.trim().isEmpty()) {
                    String[] sortParams = sort.split(",");
                    String sortField = sortParams[0].trim();
                    String sortDirection = sortParams.length > 1 ? sortParams[1].trim() : "asc";
                    Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    
                    // Only sort on keyword fields (category, brand, productId) or skip sorting
                    // Text fields (name, description) are analyzed and not directly sortable
                    if (sortField.equals("category") || sortField.equals("brand") || sortField.equals("productId")) {
                        criteriaQuery.addSort(Sort.by(direction, sortField));
                    } else if (sortField.equals("price")) {
                        // Variant price sorting - might not work with nested fields in Criteria API
                        // Skip for now to avoid errors
                        log.debug("Price sorting on nested field skipped to avoid query errors");
                    }
                    // Skip sorting on name/description (text fields) to avoid errors
                }
            } catch (Exception e) {
                log.warn("Error setting sort, skipping sort: {}", e.getMessage());
                // Skip sorting if there's an error
            }

            log.debug("Executing Elasticsearch query with criteria: {}", criteria);
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(criteriaQuery, ProductDocument.class);
            
            log.info("Elasticsearch search returned {} total hits", searchHits.getTotalHits());

            // Extract productIds from search results
            // ProductDocument.id is now the productId (business identifier)
            List<String> productIds = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(ProductDocument::getId) // This is now productId
                    .filter(id -> id != null && !id.isEmpty())
                    .collect(Collectors.toList());
            
            log.info("Extracted {} productIds from search results", productIds.size());
            if (!productIds.isEmpty()) {
                log.debug("Sample productIds: {}", productIds.subList(0, Math.min(3, productIds.size())));
            }

            // Fetch full products from MongoDB using productId
            List<Product> products = new java.util.ArrayList<>();
            int foundCount = 0;
            int notFoundCount = 0;
            
            for (String productId : productIds) {
                java.util.Optional<Product> productOpt = productRepository.findFirstByProductId(productId);
                if (productOpt.isPresent()) {
                    products.add(productOpt.get());
                    foundCount++;
                } else {
                    notFoundCount++;
                    if (notFoundCount <= 3) {
                        log.warn("Product not found in MongoDB for productId: {}", productId);
                    }
                }
            }
            
            log.info("Found {} products in MongoDB out of {} productIds ({} not found)", 
                    foundCount, productIds.size(), notFoundCount);

            List<ProductResponse> productResponses = products.stream()
                    .map(productMapper::toProductResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(productResponses, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("Error searching products in Elasticsearch: {}", e.getMessage(), e);
            // Log the full exception stack trace for debugging
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            // Return empty page on error - but log the actual error
            log.error("Returning empty results due to error. Check Elasticsearch connection and query syntax.");
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }
}

