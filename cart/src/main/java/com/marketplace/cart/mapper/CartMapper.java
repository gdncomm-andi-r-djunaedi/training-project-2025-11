package com.marketplace.cart.mapper;

import com.marketplace.cart.cache.CartCache;
import com.marketplace.cart.dto.CartItemResponse;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    CartItemResponse toItemResponse(CartItem item);

    List<CartItemResponse> toItemResponseList(List<CartItem> items);

    CartResponse toResponse(Cart cart);

    // Cache mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "lastModified")
    CartResponse cacheToResponse(CartCache cache);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    CartItemResponse cacheItemToResponse(CartCache.CartItemCache item);

    List<CartItemResponse> cacheItemsToResponseList(List<CartCache.CartItemCache> items);

    default CartResponse mapCacheToResponse(CartCache cache) {
        if (cache == null) return null;
        
        return CartResponse.builder()
                .memberId(cache.getMemberId())
                .items(cacheItemsToResponseList(cache.getItems()))
                .totalAmount(cache.getTotalAmount())
                .totalItems(cache.getTotalItems())
                .updatedAt(cache.getLastModified())
                .build();
    }
}

