package com.marketplace.product.service;

import com.marketplace.product.dto.PaginatedProductResponse;
import com.marketplace.product.dto.ProductIdsRequest;
import com.marketplace.product.dto.ProductRequest;
import com.marketplace.product.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProduct(String productId);
    List<ProductResponse> getProducts(ProductIdsRequest request);
    PaginatedProductResponse getAllProducts(Pageable pageable);
    ProductResponse updateProduct(String productId, ProductRequest request);
    void deleteProduct(String productId);
    int syncAllProductsToElasticsearch();

}
