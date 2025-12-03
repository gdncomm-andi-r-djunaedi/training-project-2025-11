package com.cart.mapper;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.response.CartResponse;
import com.cart.entity.Cart;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-02T14:47:16+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Homebrew)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartResponse toResponse(Cart cart) {
        if ( cart == null ) {
            return null;
        }

        CartResponse cartResponse = new CartResponse();

        return cartResponse;
    }

    @Override
    public Cart toEntity(AddItemRequest request) {
        if ( request == null ) {
            return null;
        }

        Cart cart = new Cart();

        return cart;
    }
}
