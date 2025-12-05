package com.project.product.service;

import com.project.product.dto.request.CreateProductRequest;
import com.project.product.dto.request.UpdateProductRequest;
import com.project.product.dto.response.PageResponse;
import com.project.product.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    /**
     * Create a new product
     */
    ProductResponse createProduct(CreateProductRequest request);

    /**
     * Get product by ID with view count increment
     */
    ProductResponse getProductById(String id);

    /**
     * Get product by SKU
     */
    ProductResponse getProductBySku(String sku);

    /**
     * Get product by slug
     */
    ProductResponse getProductBySlug(String slug);

    /**
     * Update existing product
     */
    ProductResponse updateProduct(String id, UpdateProductRequest request);

    /**
     * Delete product (soft delete by setting isActive to false)
     */
    void deleteProduct(String id);

    /**
     * Get all products with pagination
     */
    PageResponse<ProductResponse> getAllProducts(Pageable pageable);

    /**
     * Get active products only
     */
    PageResponse<ProductResponse> getActiveProducts(Pageable pageable);

    /**
     * Search products with pagination
     */
    PageResponse<ProductResponse> searchProducts(String keyword, Boolean activeOnly, Pageable pageable);

    /**
     * Get products by category
     */
    PageResponse<ProductResponse> getProductsByCategory(String category, Pageable pageable);

    /**
     * Get products by IDs (bulk fetch)
     */
    List<ProductResponse> getProductsByIds(List<String> ids);
}
