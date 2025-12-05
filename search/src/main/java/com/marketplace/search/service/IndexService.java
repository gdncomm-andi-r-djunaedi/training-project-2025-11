package com.marketplace.search.service;

import com.marketplace.search.document.ProductDocument;

public interface IndexService {
    void saveProduct(ProductDocument document);
    void deleteProduct(String productId);
}
