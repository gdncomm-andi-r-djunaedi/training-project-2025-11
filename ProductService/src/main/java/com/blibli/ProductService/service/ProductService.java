package com.blibli.ProductService.service;

import com.blibli.ProductService.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto getProductById(String productId);

    ProductDto createProduct(ProductDto productDto);

    void deleteById(String productId);


    int syncAllProductsToElasticsearch();
}
