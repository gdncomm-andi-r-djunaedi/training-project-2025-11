package com.marketplace.product.service;

import com.marketplace.product.dto.PagedResponse;
import com.marketplace.product.dto.ProductDto;
import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Value("${pagination.default-page}")
    private int defaultPage;
    
    @Value("${pagination.default-size}")
    private int defaultSize;
    
    @Value("${pagination.max-size}")
    private int maxSize;
    
    public PagedResponse<ProductDto> searchProducts(String query, Integer page, Integer size) {
        log.info("Searching products with query: {}", query);
        
        Pageable pageable = createPageable(page, size);
        Page<Product> productPage;
        
        if (query == null || query.trim().isEmpty()) {
            // If no query, return all products
            productPage = productRepository.findAll(pageable);
        } else {
            // Use wildcard search in name and description
            productPage = productRepository.searchByNameOrDescription(query.trim(), pageable);
        }
        
        return convertToPagedResponse(productPage);
    }
    
    public PagedResponse<ProductDto> getAllProducts(Integer page, Integer size) {
        log.info("Getting all products");
        
        Pageable pageable = createPageable(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        
        return convertToPagedResponse(productPage);
    }
    
    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(String id) {
        log.info("Getting product by ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        return convertToDto(product);
    }
    
    public PagedResponse<ProductDto> getProductsByCategory(String category, Integer page, Integer size) {
        log.info("Getting products by category: {}", category);
        
        Pageable pageable = createPageable(page, size);
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        
        return convertToPagedResponse(productPage);
    }
    
    private Pageable createPageable(Integer page, Integer size) {
        int pageNumber = (page != null && page >= 0) ? page : defaultPage;
        int pageSize = (size != null && size > 0) ? Math.min(size, maxSize) : defaultSize;
        
        return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    private PagedResponse<ProductDto> convertToPagedResponse(Page<Product> productPage) {
        List<ProductDto> products = productPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return PagedResponse.<ProductDto>builder()
                .content(products)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }
    
    private ProductDto convertToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
