package com.blibli.training.product.service;

import com.blibli.training.product.dto.ProductRequest;
import com.blibli.training.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse findByName(String name);

    List<ProductResponse> getAllProduct();

    List<ProductResponse> searchProduct(String query, int page, int size);
}
