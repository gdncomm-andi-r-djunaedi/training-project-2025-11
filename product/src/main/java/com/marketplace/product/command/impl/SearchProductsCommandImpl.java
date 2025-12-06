package com.marketplace.product.command.impl;

import com.marketplace.product.command.SearchProductsCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.SearchProductsRequest;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchProductsCommandImpl implements SearchProductsCommand {

    private final ProductRepository productRepository;
    private final com.marketplace.common.mapper.MapperService mapperService;

    @Override
    public Page<com.marketplace.product.dto.response.ProductResponse> execute(SearchProductsRequest request) {
        var name = request.getName();
        var pageable = request.getPageable();

        log.info("Searching products with name containing: '{}', page: {}", name, pageable.getPageNumber());

        Page<Product> results;
        if (name == null || name.trim().isEmpty()) {
            log.debug("No search term provided, returning all products");
            results = productRepository.findAll(pageable);
        } else {
            results = productRepository.findByNameContainingIgnoreCase(name, pageable);
        }

        log.info("Found {} products matching search term", results.getTotalElements());
        return results
                .map(product -> mapperService.map(product, com.marketplace.product.dto.response.ProductResponse.class));
    }
}
