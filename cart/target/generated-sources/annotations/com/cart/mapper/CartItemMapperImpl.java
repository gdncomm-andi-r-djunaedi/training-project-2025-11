package com.cart.mapper;

import com.cart.dto.response.CartItemResponse;
import com.cart.entity.CartItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-02T14:47:16+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Homebrew)"
)
@Component
public class CartItemMapperImpl implements CartItemMapper {

    @Override
    public CartItemResponse toResponse(CartItem item) {
        if ( item == null ) {
            return null;
        }

        CartItemResponse cartItemResponse = new CartItemResponse();

        return cartItemResponse;
    }
}
