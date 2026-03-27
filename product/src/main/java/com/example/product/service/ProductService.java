package com.example.product.service;

import com.example.product.dto.ProductListResponse;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.dto.UpdateProductRequest;
import com.example.product.entity.ProductEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductResponse> addProducts(List<ProductRequest> productRequests);
    ProductListResponse getProductsListing(int pageNumber, int pageSize);
    ProductResponse getProductDetailByItemSku(String itemSku);
    ProductListResponse getProductsBySearchTerm(String searchTerm, int pageNumber, int pageSize);
    String buildWildcardRegex(String searchTerm);
    ProductListResponse buildProductListResponse(Page<ProductEntity> productPage);
    ProductEntity convertToEntity(ProductRequest request);
    ProductResponse convertToResponse(ProductEntity entity);
    ProductResponse updateProduct(String itemSku, UpdateProductRequest updateRequest) throws Exception;
    void deleteProductByItemSku(String itemSku) throws Exception;
}
