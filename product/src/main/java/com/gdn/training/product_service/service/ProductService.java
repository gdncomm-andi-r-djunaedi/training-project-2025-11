package com.gdn.training.product_service.service;

import com.gdn.training.product_service.dto.ProductResponse;
import com.gdn.training.product_service.entity.Product;
import com.gdn.training.product_service.exception.ProductNotFoundException;
import com.gdn.training.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable){
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<ProductResponse> searchProducts(String query, Pageable pageable){
        return productRepository.searchByName(query, pageable)
                .map(this::toResponse);
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

//Convert Product entity to ProductResponse DTO
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
