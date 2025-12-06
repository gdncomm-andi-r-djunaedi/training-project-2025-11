package com.gdn.training.product.infrastructure.persistence.mapper;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.infrastructure.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "productId", source = "productId", qualifiedByName = "stringToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "stringToUuid")
    Product toDomain(ProductEntity entity);

    @Mapping(target = "productId", source = "productId", qualifiedByName = "uuidToString")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToString")
    ProductEntity toEntity(Product domain);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String id) {
        return id != null ? UUID.fromString(id) : null;
    }
}