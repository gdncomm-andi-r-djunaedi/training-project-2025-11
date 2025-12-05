package com.marketplace.product.service;

import com.marketplace.product.dto.ProductIdsRequest;
import com.marketplace.product.dto.ProductRequest;
import com.marketplace.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProduct(String productId);
    List<ProductResponse> getProducts(ProductIdsRequest request);
    ProductResponse updateProduct(String productId, ProductRequest request);
    void deleteProduct(String productId);
    int syncAllProductsToElasticsearch();

}
