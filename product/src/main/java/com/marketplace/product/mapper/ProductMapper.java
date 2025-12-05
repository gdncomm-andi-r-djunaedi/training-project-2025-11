package com.marketplace.product.mapper;

import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    ProductResponse.ProductSpecsDto toSpecsDto(Product.ProductSpecs specs);
}

