package org.jovian.product.service;

import org.jovian.product.entity.Product;
import org.jovian.product.dto.ProductRequest;
import org.jovian.product.dto.ProductDetailsResponse;
import org.jovian.common.dto.ProductDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService{

    ProductDTO createProduct(ProductRequest productRequest);
    String generateProductCode();
    String generateVariationCode();
    ProductDetailsResponse viewProductDetails(String productId);
    Page<Product> findAll(Pageable pageable);
    Page<Product> searchProducts(String keyword, Pageable pageable);
    ProductDTO toDTO(Product product);
    Product findById(String id);
}
