package com.cart.mapper;

import com.cart.dto.response.CartItemResponse;
import com.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemResponse toResponse(CartItem item);
}
