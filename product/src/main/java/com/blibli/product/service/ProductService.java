package com.blibli.product.service;

import com.blibli.product.dto.PageResponse;
import com.blibli.product.dto.ProductRequest;
import com.blibli.product.dto.ProductResponse;
import com.blibli.product.enums.CategoryType;

public interface ProductService {
    public PageResponse<ProductResponse> getAllProducts(int page, int size);
    public PageResponse<ProductResponse> getProductsByCategory(CategoryType category, int page, int size);
    public ProductResponse getProductById(String id);
    public ProductResponse getProductBySku(String sku);
    public ProductResponse createProduct(ProductRequest request);
    public ProductResponse updateProduct(String id, ProductRequest request);
    public void deleteProduct(String id);
}
