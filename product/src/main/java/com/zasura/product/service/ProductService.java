package com.zasura.product.service;

import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;

import java.util.List;

public interface ProductService {
  Product getProductDetail(String productId);

  Product createProduct(Product product);

  List<Product> searchProducts(ProductSearchRequest productSearchRequest);
}
