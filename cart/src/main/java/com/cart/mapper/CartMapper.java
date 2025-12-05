package com.cart.mapper;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.response.CartResponse;
import com.cart.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    CartResponse toResponse(Cart cart);

    Cart toEntity(AddItemRequest request);
}
