package com.marketplace.product.service;

import com.marketplace.common.dto.PageInfo;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.dto.ProductSearchRequest;
import com.marketplace.product.entity.Product;
import com.marketplace.product.mapper.ProductMapper;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse getProductById(String id) {
        log.info("Getting product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        return productMapper.toResponse(product);
    }

    public List<ProductResponse> getProductsByIds(List<String> ids) {
        log.info("Getting products by ids: {}", ids);
        List<Product> products = productRepository.findByIdIn(ids);
        return productMapper.toResponseList(products);
    }

    public Page<ProductResponse> listProducts(int page, int size, String sortBy, String sortDirection) {
        log.info("Listing products - page: {}, size: {}", page, size);
        
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);
        return productPage.map(productMapper::toResponse);
    }

    public Page<ProductResponse> searchProducts(ProductSearchRequest request) {
        log.info("Searching products with keyword: {}", request.getKeyword());
        
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<Product> productPage;
        
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            // Wildcard search using regex
            String searchPattern = ".*" + escapeRegex(request.getKeyword()) + ".*";
            productPage = productRepository.searchProducts(searchPattern, pageable);
        } else if (request.getCategory() != null && !request.getCategory().isBlank()) {
            productPage = productRepository.findByCategoryAndActiveTrue(request.getCategory(), pageable);
        } else if (request.getBrand() != null && !request.getBrand().isBlank()) {
            productPage = productRepository.findByBrandAndActiveTrue(request.getBrand(), pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }
        
        return productPage.map(productMapper::toResponse);
    }

    public PageInfo createPageInfo(Page<?> page) {
        return PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }
        
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        
        return Sort.by(direction, sortBy);
    }

    private String escapeRegex(String input) {
        // Escape special regex characters for safe wildcard search
        return input.replaceAll("([\\\\^$.|?*+()\\[\\]{}])", "\\\\$1");
    }
}

