package com.kailash.search.service;

import com.kailash.search.dto.ProductEvent;

public interface ProductIndexerService {

    void handleEvent(ProductEvent event);

    void upsertProduct(ProductEvent event);

    void deleteProduct(String productId);
}
