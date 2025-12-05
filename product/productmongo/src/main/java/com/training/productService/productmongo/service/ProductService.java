package com.training.productService.productmongo.service;

import com.training.productService.productmongo.dto.ProductDTO;
import com.training.productService.productmongo.dto.ProductPageResponse;

public interface ProductService {

    ProductPageResponse searchProducts(String searchTerm, int page, int size) throws Exception;
    ProductDTO getProductBySku(String sku);
    ProductDTO createProduct(ProductDTO request);
    void deleteProductBySku(String sku);
}
