package com.microservice.search.service.serviceImpl;

import com.microservice.search.dto.ProductEventDto;
import com.microservice.search.dto.ProductResponseDto;
import com.microservice.search.entity.ProductDocument;
import com.microservice.search.repository.ProductSearchRepository;
import com.microservice.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void indexProduct(ProductEventDto productEventDto) {
        try {
            log.info("Indexing product with SKU ID: {}", productEventDto.getSkuId());
            ProductDocument productDocument = convertToDocument(productEventDto);
            productSearchRepository.save(productDocument);
            log.info("Successfully indexed product with SKU ID: {}", productEventDto.getSkuId());
        } catch (Exception e) {
            log.error("Error indexing product with SKU ID: {}", productEventDto.getSkuId(), e);
            throw new RuntimeException("Failed to index product: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProduct(ProductEventDto productEventDto) {
        try {
            log.info("Updating product with SKU ID: {}", productEventDto.getSkuId());
            ProductDocument productDocument = convertToDocument(productEventDto);
            productSearchRepository.save(productDocument);
            log.info("Successfully updated product with SKU ID: {}", productEventDto.getSkuId());
        } catch (Exception e) {
            log.error("Error updating product with SKU ID: {}", productEventDto.getSkuId(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduct(String skuId) {
        try {
            log.info("Deleting product with SKU ID: {}", skuId);
            productSearchRepository.deleteById(skuId);
            log.info("Successfully deleted product with SKU ID: {}", skuId);
        } catch (Exception e) {
            log.error("Error deleting product with SKU ID: {}", skuId, e);
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String query, Pageable pageable) {
        try {
            log.info("Performing full-text search with query: {}", query);

            String normalizedQuery = normalizeQuery(query);

            Criteria criteria = new Criteria()
                    .or(new Criteria("name").contains(normalizedQuery))
                    .or(new Criteria("description").contains(normalizedQuery));

            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria).setPageable(pageable);

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                    criteriaQuery, ProductDocument.class);

            List<ProductDocument> products = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            List<ProductResponseDto> productDtos = products.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

            log.info("Found {} products matching query: {}", productDtos.size(), query);

            return new PageImpl<>(
                    productDtos,
                    pageable,
                    searchHits.getTotalHits()
            );
        } catch (Exception e) {
            log.error("Error performing search with query: {}", query, e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ProductResponseDto> searchByCategory(String category, Pageable pageable) {
        try {
            log.info("Searching products by category: {}", category);

            String normalizedCategory = normalizeQuery(category);
            Criteria criteria = new Criteria("category").is(normalizedCategory);
            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria).setPageable(pageable);

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                    criteriaQuery, ProductDocument.class);

            List<ProductDocument> products = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            List<ProductResponseDto> productDtos = products.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

            log.info("Found {} products in category: {}", productDtos.size(), category);

            return new PageImpl<>(
                    productDtos,
                    pageable,
                    searchHits.getTotalHits()
            );
        } catch (Exception e) {
            log.error("Error searching products by category: {}", category, e);
            throw new RuntimeException("Failed to search products by category: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ProductResponseDto> searchByBrand(String brand, Pageable pageable) {
        try {
            log.info("Searching products by brand: {}", brand);

            String normalizedBrand = normalizeQuery(brand);
            Criteria criteria = new Criteria("brand").is(normalizedBrand);
            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria).setPageable(pageable);

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                    criteriaQuery, ProductDocument.class);

            List<ProductDocument> products = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            List<ProductResponseDto> productDtos = products.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

            log.info("Found {} products for brand: {}", productDtos.size(), brand);

            return new PageImpl<>(
                    productDtos,
                    pageable,
                    searchHits.getTotalHits()
            );
        } catch (Exception e) {
            log.error("Error searching products by brand: {}", brand, e);
            throw new RuntimeException("Failed to search products by brand: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<ProductResponseDto> comprehensiveSearch(String query, Pageable pageable) {
        try {
            log.info("Performing comprehensive search with query: {}", query);

            String normalizedQuery = normalizeQuery(query);

            Criteria criteria = new Criteria()
                    .or(new Criteria("name").contains(normalizedQuery))
                    .or(new Criteria("description").contains(normalizedQuery))
                    .or(new Criteria("category").is(normalizedQuery))
                    .or(new Criteria("brand").is(normalizedQuery));

            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria).setPageable(pageable);

            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                    criteriaQuery, ProductDocument.class);

            List<ProductDocument> products = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            List<ProductResponseDto> productDtos = products.stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());

            log.info("Found {} products matching comprehensive search query: {}", productDtos.size(), query);

            return new PageImpl<>(
                    productDtos,
                    pageable,
                    searchHits.getTotalHits()
            );
        } catch (Exception e) {
            log.error("Error performing comprehensive search with query: {}", query, e);
            throw new RuntimeException("Failed to perform comprehensive search: " + e.getMessage(), e);
        }
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        return query.toLowerCase().replaceAll("\\s+", "");
    }


    private ProductDocument convertToDocument(ProductEventDto dto) {
        ProductDocument document = new ProductDocument();
        document.setSkuId(dto.getSkuId());
        document.setStoreId(dto.getStoreId());

        document.setName(dto.getName() != null ?
                normalizeQuery(dto.getName()) : null);

        document.setDescription(dto.getDescription() != null ?
                normalizeQuery(dto.getDescription()) : null);

        document.setCategory(dto.getCategory() != null ?
                normalizeQuery(dto.getCategory()) : null);

        document.setBrand(dto.getBrand() != null ?
                normalizeQuery(dto.getBrand()) : null);

        document.setPrice(dto.getPrice());
        document.setItemCode(dto.getItemCode());
        document.setIsActive(dto.getIsActive());
        document.setLength(dto.getLength());
        document.setHeight(dto.getHeight());
        document.setWidth(dto.getWidth());
        document.setWeight(dto.getWeight());
        document.setDangerousLevel(dto.getDangerousLevel());
        document.setCreatedAt(
                dto.getCreatedAt() != null ? dto.getCreatedAt().toLocalDate() : null
        );
        document.setUpdatedAt(
                dto.getUpdatedAt() != null ? dto.getUpdatedAt().toLocalDate() : null
        );
        return document;
    }

    private ProductResponseDto convertToResponseDto(ProductDocument document) {
        if (document == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setSkuId(document.getSkuId());
        dto.setStoreId(document.getStoreId());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        dto.setCategory(document.getCategory());
        dto.setBrand(document.getBrand());
        dto.setPrice(document.getPrice());
        dto.setItemCode(document.getItemCode());
        dto.setIsActive(document.getIsActive());
        dto.setLength(document.getLength());
        dto.setHeight(document.getHeight());
        dto.setWidth(document.getWidth());
        dto.setWeight(document.getWeight());
        dto.setDangerousLevel(document.getDangerousLevel());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }
}