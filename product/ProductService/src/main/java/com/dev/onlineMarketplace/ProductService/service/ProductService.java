package com.dev.onlineMarketplace.ProductService.service;

import com.dev.onlineMarketplace.ProductService.dto.ProductDTO;
import com.dev.onlineMarketplace.ProductService.dto.ProductSearchResponse;

public interface ProductService {
    ProductSearchResponse searchProducts(String query, int page, int limit);

    ProductDTO getProductByIdOrSku(String identifier);
}
