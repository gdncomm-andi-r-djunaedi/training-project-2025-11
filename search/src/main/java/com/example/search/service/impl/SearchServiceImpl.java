package com.example.search.service.impl;

import com.example.search.dto.ProductDocumentDto;
import com.example.search.dto.SearchResponse;
import com.example.search.entity.ProductDocument;
import com.example.search.exceptions.SearchException;
import com.example.search.repository.ProductRepository;
import com.example.search.service.SearchService;
import com.example.search.utils.PaginationMetadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public SearchResponse search(String term, Pageable pageable) {
        validateSearchTerm(term);
        
        try {
            Page<ProductDocument> page = productRepository.wildcardSearch(term, pageable);
            return buildSearchResponse(page, pageable);
        } catch (RuntimeException e) {
            throw new SearchException("Failed to perform search: " + e.getMessage());
        }
    }

    @Override
    public SearchResponse searchWithPriority(String term, Pageable pageable) {
        validateSearchTerm(term);
        
        try {
            Page<ProductDocument> page = productRepository.wildcardSearchWithPriority(term, pageable);
            return buildSearchResponse(page, pageable);
        } catch (RuntimeException e) {
            throw new SearchException("Failed to perform priority search: " + e.getMessage());
        }
    }

    @Override
    public SearchResponse searchWithPriorityAndPriceSort(String term, Pageable pageable, String sortOrder) {
        validateSearchTerm(term);
        validateSortOrder(sortOrder);
        
        try {
            Page<ProductDocument> page = productRepository.wildcardSearchWithPriorityAndPriceSort(term, pageable, sortOrder);
            return buildSearchResponse(page, pageable);
        } catch (RuntimeException e) {
            throw new SearchException("Failed to perform priority search with price sort: " + e.getMessage());
        }
    }

    private void validateSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            throw new SearchException("Search term cannot be null or empty");
        }
        if (term.length() < 2) {
            throw new SearchException("Search term must be at least 2 characters long");
        }
    }

    private void validateSortOrder(String sortOrder) {
        if (sortOrder != null && !sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new SearchException("Sort order must be either 'asc' or 'desc'");
        }
    }

    private SearchResponse buildSearchResponse(Page<ProductDocument> page, Pageable pageable) {
        List<ProductDocumentDto> dtoList = page.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PaginationMetadata metadata = PaginationMetadata.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                page.getNumberOfElements(),
                page.getTotalPages()
        );

        SearchResponse response = new SearchResponse();
        response.setProducts(dtoList);
        response.setMetadata(metadata);
        return response;
    }

    private ProductDocumentDto mapToDTO(ProductDocument product) {
        return ProductDocumentDto.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .markForDelete(product.isMarkForDelete())
                .build();
    }
}
