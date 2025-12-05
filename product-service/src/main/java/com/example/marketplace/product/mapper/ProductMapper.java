package com.example.marketplace.product.mapper;

import com.example.marketplace.product.domain.Product;
import com.example.marketplace.product.dto.ProductRequestDTO;
import com.example.marketplace.product.dto.ProductResponseDTO;

public class ProductMapper {
    public static Product toEntity(ProductRequestDTO dto) {
        return new Product(dto.getId(), dto.getName(), dto.getDescription(), dto.getPrice());
    }
    public static ProductResponseDTO toDto(Product p) {
        return new ProductResponseDTO(p.getId(), p.getName(), p.getDescription(), p.getPrice());
    }
}
