package com.gdn.training.product.service;

import com.gdn.training.product.model.entity.Product;
import com.gdn.training.product.model.response.PagedResponse;
import com.gdn.training.product.model.response.ProductDetailResponse;
import com.gdn.training.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private ProductDetailResponse toResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }

    private PagedResponse<ProductDetailResponse> toPagedResponse(Page<Product> products) {
        return PagedResponse.<ProductDetailResponse>builder()
                .page(products.getPageable().getPageNumber())
                .size(products.getPageable().getPageSize())
                .totalPages(products.getTotalPages())
                .totalElements(products.getTotalElements())
                .content(products.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    public PagedResponse<ProductDetailResponse> search(String name, int page, int size) {
        Page<Product> products;

        if (name == null || name.isBlank()) {
            products = productRepository.findAll(PageRequest.of(page, size));
        } else {
            Pattern pattern = Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE);
            products = productRepository.findByNameRegex(pattern, PageRequest.of(page, size));
        }

        return toPagedResponse(products);

    }

    public PagedResponse<ProductDetailResponse> getAll(int page, int size) {
        Page<Product> products = productRepository.findAll(PageRequest.of(page, size));
        return toPagedResponse(products);
    }

    public ProductDetailResponse getDetail(@NonNull Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

}
