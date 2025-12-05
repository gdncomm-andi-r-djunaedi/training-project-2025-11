package com.blibli.gdn.productService.service;

import com.blibli.gdn.productService.model.Product;

public interface ProductIndexingService {
    void indexProduct(Product product);
    void deleteProduct(String productId);
    void deleteProductById(String mongoId); // Delete by MongoDB _id
    void updateProduct(Product product); // Update existing product in index
    void reindexAllProducts();
}

