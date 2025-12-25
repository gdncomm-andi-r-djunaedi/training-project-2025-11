package com.blibi.product.service;

import com.blibi.product.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Service interface for Product business logic operations.
  // This interface defines the contract for product-related operations, following

public interface ProductService {

    ProductDTO createProduct(ProductDTO productDTO);
    Page<ProductDTO> viewProductDetailsByName(String productName, Pageable pageable);
    Page<ProductDTO> searchProductByName(String productName, Pageable pageable);
    ProductDTO getProductDetail(String productId);
    Page<ProductDTO> searchProductByCategory(String category, Pageable pageable);
}
