package com.gdn.project.waroenk.catalog.service;

import com.gdn.project.waroenk.catalog.FilterProductRequest;
import com.gdn.project.waroenk.catalog.MultipleProductResponse;
import com.gdn.project.waroenk.catalog.entity.Product;

public interface ProductService {
  Product createProduct(Product product);
  Product updateProduct(String id, Product product);
  Product findProductById(String id);
  Product findProductBySku(String sku);
  boolean deleteProduct(String id);
  MultipleProductResponse filterProducts(FilterProductRequest request);
}






