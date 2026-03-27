package com.example.api_gateway.service;

import com.example.api_gateway.response.ProductListResponse;
import com.example.api_gateway.request.ProductRequest;
import com.example.api_gateway.response.ProductResponse;
import com.example.api_gateway.request.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    List<ProductResponse> addproducts(List<ProductRequest> productRequests);
    ProductListResponse getProductsListing(int pageNumber, int size);
    ProductResponse getProductDetailByItemSku(String itemSku);
    ProductListResponse getProductBySearchTerm(String searchTerm, int pageNumber,int pageSize);
    ProductResponse updateProduct(String itemSku, UpdateProductRequest updateRequest);
    void deleteproductByItemSku(String itemSku);
}
