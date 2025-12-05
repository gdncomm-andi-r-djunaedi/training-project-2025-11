package com.blibli.product.service;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.CreateProductResponseDTO;

import java.util.List;

public interface ProductService {
    List<CreateProductResponseDTO> createProduct(List<CreateProductRequestDTO> createProductRequestDTOlist);

    CreateProductResponseDTO findProductById(String productId);

    CreateProductResponseDTO updateProductData(CreateProductRequestDTO createProductRequestDTO);

    List<CreateProductResponseDTO> findProductByIdList(List<String> productIds);
}
