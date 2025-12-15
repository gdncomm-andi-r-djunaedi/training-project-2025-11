package com.project.product.mapper;


import com.project.product.dto.ProductImageDto;
import com.project.product.dto.request.CreateProductRequest;
import com.project.product.dto.request.UpdateProductRequest;
import com.project.product.dto.response.ProductResponse;
import com.project.product.entity.Product;
import com.project.product.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "slug", ignore = true) // Will be generated from name
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "rating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "reviewCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Product toEntity(CreateProductRequest request);

    ProductResponse  toResponse(Product product);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true) // SKU cannot be updated
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    ProductImage toProductImage(ProductImageDto dto);
    ProductImageDto toProductImageDto(ProductImage entity);
}
