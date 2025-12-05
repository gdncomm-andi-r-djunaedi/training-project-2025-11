package com.marketplace.search.service.impl;

import com.marketplace.search.document.ProductDocument;
import com.marketplace.search.dto.SearchResponse;
import com.marketplace.search.repository.ProductDocumentRepository;
import com.marketplace.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProductDocumentRepository productDocumentRepository;

    @Override
    public SearchResponse search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        Page<ProductDocument> results = productDocumentRepository
                .searchByTitleOrDescription(query, pageable);

        if (results.getTotalElements() == 0 && productDocumentRepository.existsById(query)) {
            ProductDocument product = productDocumentRepository.findById(query).orElse(null);
            if (product != null) {
                results = new org.springframework.data.domain.PageImpl<>(List.of(product), pageable, 1);
            }
        }

        log.info("Search completed for query: '{}', found {} results", query, results.getTotalElements());

        return SearchResponse.builder()
                .content(results.getContent().stream()
                        .map(this::mapToSearchResult)
                        .collect(Collectors.toList()))
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .build();
    }

    private SearchResponse.ProductSearchResult mapToSearchResult(ProductDocument document) {
        SearchResponse.ProductSearchResult result = new SearchResponse.ProductSearchResult();
        BeanUtils.copyProperties(document, result);
        return result;
    }
}
