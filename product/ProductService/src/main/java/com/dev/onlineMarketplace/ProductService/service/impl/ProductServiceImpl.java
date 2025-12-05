package com.dev.onlineMarketplace.ProductService.service.impl;

import com.dev.onlineMarketplace.ProductService.dto.ProductDTO;
import com.dev.onlineMarketplace.ProductService.dto.ProductSearchResponse;
import com.dev.onlineMarketplace.ProductService.model.Product;
import com.dev.onlineMarketplace.ProductService.repository.ProductRepository;
import com.dev.onlineMarketplace.ProductService.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductSearchResponse searchProducts(String query, int page, int limit) {
        // Adjust page to 0-indexed for Spring Data
        int pageNo = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, limit);

        Page<Product> productPage;
        if (query == null || query.isEmpty()) {
            productPage = productRepository.findAll(pageable);
        } else {
            // Remove wildcard characters if present, as regex handles it
            String keyword = query.replace("*", "");
            productPage = productRepository.searchByNameOrDescription(keyword, pageable);
        }

        List<ProductDTO> items = productPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new ProductSearchResponse(
                page,
                limit,
                productPage.getTotalElements(),
                items);
    }

    @Override
    public ProductDTO getProductByIdOrSku(String identifier) {
        // Try to find by ID first
        Product product = productRepository.findById(identifier)
                .orElseGet(() ->
                // If not found by ID, try to find by SKU
                productRepository.findBySku(identifier)
                        .orElseThrow(() -> new RuntimeException("Product not found with ID or SKU: " + identifier)));
        return mapToDTO(product);
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl());
    }
}
