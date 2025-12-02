package com.example.product.service;

import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    
    ProductResponseDTO getProductById(String id);
    
    ProductResponseDTO getProductByProductId(long productId);
    
    List<ProductResponseDTO> getAllProducts();
    
    List<ProductResponseDTO> getProductsByCategory(String category);
    
    List<ProductResponseDTO> searchProductsByTitle(String title);
    
    ProductResponseDTO updateProduct(String id, ProductRequestDTO updateProductDTO);
    
    void deleteProduct(String id);

    void deleteProductByProductId(long id);
}

