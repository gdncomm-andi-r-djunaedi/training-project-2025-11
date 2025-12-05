package com.project.product.service.impl;
import com.project.product.dto.request.CreateProductRequest;
import com.project.product.dto.request.UpdateProductRequest;
import com.project.product.dto.response.PageResponse;
import com.project.product.dto.response.ProductResponse;
import com.project.product.entity.Product;
import com.project.product.exception.DuplicateSkuException;
import com.project.product.exception.ProductNotFoundException;
import com.project.product.mapper.ProductMapper;
import com.project.product.repository.ProductRepository;
import com.project.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        // Check for duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        // Map request to entity
        Product product = productMapper.toEntity(request);

        // Generate slug from name
        product.setSlug(generateSlug(request.getName()));

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(String id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id));

        // Increment view count
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "'sku:' + #sku")
    public ProductResponse getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("SKU", sku));

        return productMapper.toResponse(product);
    }

    @Override
    @Cacheable(value = "products", key = "'slug:' + #slug")
    public ProductResponse getProductBySlug(String slug) {
        log.info("Fetching product with slug: {}", slug);

        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("slug", slug));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id));

        // Update fields using mapper (only non-null fields)
        productMapper.updateEntityFromRequest(request, product);

        // Update slug if name changed
        if (StringUtils.hasText(request.getName())) {
            product.setSlug(generateSlug(request.getName()));
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", id);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public void deleteProduct(String id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id));

        // Soft delete
        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product soft deleted successfully: {}", id);
    }

    @Override
    @Cacheable(value = "productList", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage = productRepository.findAll(pageable);
        return convertToPageResponse(productPage);
    }

    @Override
    @Cacheable(value = "productList", key = "'active:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public PageResponse<ProductResponse> getActiveProducts(Pageable pageable) {
        log.info("Fetching active products - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> productPage = productRepository.findByIsActive(true, pageable);
        return convertToPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(String keyword, Boolean activeOnly,
                                                        Pageable pageable) {
        log.info("Searching products with keyword: '{}', activeOnly: {}", keyword, activeOnly);

        Page<Product> productPage;
        if (activeOnly != null && activeOnly) {
            productPage = productRepository.searchActiveProducts(keyword, true, pageable);
        } else {
            productPage = productRepository.searchProducts(keyword, pageable);
        }

        return convertToPageResponse(productPage);
    }

    @Override
    @Cacheable(value = "productList", key = "'category:' + #category + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public PageResponse<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        log.info("Fetching products by category: {}", category);

        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        return convertToPageResponse(productPage);
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<String> ids) {
        log.info("Fetching products by IDs: {}", ids);

        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    private PageResponse<ProductResponse> convertToPageResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Generate URL-friendly slug from product name
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
