package com.gdn.training.product.service;

import com.gdn.training.product.model.entity.Product;
import com.gdn.training.product.model.response.PagedResponse;
import com.gdn.training.product.model.response.ProductDetailResponse;
import com.gdn.training.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public PagedResponse<ProductDetailResponse> getAll(String name, Pageable pageable) {
        Page<Product> products;

        if (name == null || name.isBlank()) {
            products = productRepository.findAll(pageable);
        } else {
            Pattern pattern = Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE);
            products = productRepository.findByNameRegex(pattern, pageable);
        }

        return PagedResponse.<ProductDetailResponse>builder()
                .page(products.getPageable().getPageNumber())
                .size(products.getPageable().getPageSize())
                .totalPages(products.getTotalPages())
                .totalElements(products.getTotalElements())
                .content(products.getContent().stream()
                        .map(p -> ProductDetailResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .price(p.getPrice())
                                .description(p.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public ProductDetailResponse getDetail(@NonNull String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }
}
