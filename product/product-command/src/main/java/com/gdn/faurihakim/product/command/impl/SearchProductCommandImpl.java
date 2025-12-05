package com.gdn.faurihakim.product.command.impl;

import com.gdn.faurihakim.Product;
import com.gdn.faurihakim.ProductRepository;
import com.gdn.faurihakim.product.command.SearchProductCommand;
import com.gdn.faurihakim.product.command.model.SearchProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.GetProductWebResponse;
import com.gdn.faurihakim.product.web.model.response.SearchProductWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchProductCommandImpl implements SearchProductCommand {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public SearchProductWebResponse execute(SearchProductCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Product> productPage;

        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            productPage = productRepository.findByProductNameContainingIgnoreCase(request.getQuery(), pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        List<GetProductWebResponse> products = productPage.getContent().stream()
                .map(product -> {
                    GetProductWebResponse response = new GetProductWebResponse();
                    BeanUtils.copyProperties(product, response);
                    return response;
                })
                .collect(Collectors.toList());

        return SearchProductWebResponse.builder()
                .products(products)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(productPage.getNumber())
                .build();
    }
}
