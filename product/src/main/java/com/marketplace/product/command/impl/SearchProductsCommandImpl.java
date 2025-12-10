package com.marketplace.product.command.impl;

import com.marketplace.product.command.SearchProductsCommand;

import com.marketplace.product.dto.request.SearchProductsRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchProductsCommandImpl implements SearchProductsCommand {

    private final com.marketplace.product.repository.ProductSearchRepository productSearchRepository;
    private final com.marketplace.common.mapper.MapperService mapperService;

    @Override
    public Page<com.marketplace.product.dto.response.ProductResponse> execute(SearchProductsRequest request) {
        var name = request.getName();
        var pageable = request.getPageable();

        log.info("Searching products in ElasticSearch with name containing: '{}', page: {}", name,
                pageable.getPageNumber());

        Page<com.marketplace.product.document.ProductSearchDoc> results;
        if (name == null || name.trim().isEmpty()) {
            log.debug("No search term provided, returning all products");
            results = productSearchRepository.findAll(pageable);
        } else {
            results = productSearchRepository.findByNameOrDescriptionContaining(name, name, pageable);
        }

        log.info("Found {} products matching search term", results.getTotalElements());
        return results
                .map(product -> mapperService.map(product, com.marketplace.product.dto.response.ProductResponse.class));
    }
}
