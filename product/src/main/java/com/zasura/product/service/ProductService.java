package com.zasura.product.service;

import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Product getProductDetail(String productId);

  Product createProduct(Product product);

  Page<Product> searchProducts(ProductSearchRequest productSearchRequest);

  Boolean migrate();
}
