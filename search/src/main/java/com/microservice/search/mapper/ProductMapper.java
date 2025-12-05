package com.microservice.search.mapper;

import com.microservice.search.dto.ProductResponseDto;
import com.microservice.search.entity.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDto toResponseDto(ProductDocument document) {
        if (document == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setSkuId(document.getSkuId());
        dto.setStoreId(document.getStoreId());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        dto.setCategory(document.getCategory());
        dto.setBrand(document.getBrand());
        dto.setPrice(document.getPrice());
        dto.setItemCode(document.getItemCode());
        dto.setIsActive(document.getIsActive());
        dto.setLength(document.getLength());
        dto.setHeight(document.getHeight());
        dto.setWidth(document.getWidth());
        dto.setWeight(document.getWeight());
        dto.setDangerousLevel(document.getDangerousLevel());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        return dto;
    }
}