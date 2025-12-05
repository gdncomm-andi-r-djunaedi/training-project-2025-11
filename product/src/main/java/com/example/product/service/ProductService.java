package com.example.product.service;

import com.example.product.dto.GetBulkProductResponseDTO;
import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    
    ProductResponseDTO getProductByProductId(long productId);
    
    List<ProductResponseDTO> getProductsByCategory(String category);
    
    List<ProductResponseDTO> searchProductsByTitle(String title);
    
    ProductResponseDTO updateProduct(long productId, ProductRequestDTO updateProductDTO);

    String deleteProductByProductId(long id);

    List<GetBulkProductResponseDTO> getProductsInBulk(List<Long> productIds);

}

