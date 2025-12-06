package com.gdn.training.cart.infrasturcture.mapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.gdn.training.cart.domain.model.Cart;
import com.gdn.training.cart.domain.model.CartItem;
import com.gdn.training.cart.infrasturcture.entity.CartEntity;
import com.gdn.training.cart.infrasturcture.entity.CartItemEntity;

/**
 * Mapper converting JPA entities to domain aggregates and vice-versa.
 * Kept in infrastructure layer; domain layer remains framework-free.
 */
@Component
public class CartEntityMapper {

    public Cart toDomain(CartEntity entity) {
        if (entity == null) {
            return null;
        }

        List<CartItem> items = entity.getItems().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        Instant createdAt = entity.getCreatedAt() == null ? Instant.now() : entity.getCreatedAt();
        Instant updatedAt = entity.getUpdatedAt() == null ? createdAt : entity.getUpdatedAt();

        return new Cart(
                entity.getId(),
                entity.getMemberId(),
                createdAt,
                updatedAt,
                items);
    }

    private CartItem toDomain(CartItemEntity e) {
        return new CartItem(
                e.getId(),
                e.getProductId(),
                e.getQuantity());
    }

    public CartEntity toEntity(Cart domain) {
        if (domain == null) {
            return null;
        }

        CartEntity entity = CartEntity.builder()
                .id(domain.getId())
                .memberId(domain.getMemberId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        // Map items and set bi-directional relation
        List<CartItemEntity> itemEntities = domain.getItems().stream()
                .map(ci -> {
                    CartItemEntity ite = CartItemEntity.builder()
                            .id(ci.getId())
                            .productId(ci.getProductId())
                            .quantity(ci.getQuantity())
                            .build();
                    ite.setCart(entity);
                    return ite;
                })
                .collect(Collectors.toList());

        entity.setItems(itemEntities);
        return entity;
    }
}
