package com.blibli.productData.services;

import com.blibli.productData.dto.ProductDTO;
import com.blibli.productData.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductService {

    Page<ProductDTO> getAllProductList(Pageable pageable);

    Page<ProductDTO> queryProducts(String searchTerm, Pageable pageable);

    ProductDTO getProductDetail(String productId);
}
