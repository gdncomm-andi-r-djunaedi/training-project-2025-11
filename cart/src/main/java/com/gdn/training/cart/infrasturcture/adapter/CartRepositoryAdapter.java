package com.gdn.training.cart.infrasturcture.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.gdn.training.cart.application.port.out.CartRepositoryPort;
import com.gdn.training.cart.domain.model.Cart;
import com.gdn.training.cart.infrasturcture.mapper.CartEntityMapper;
import com.gdn.training.cart.infrasturcture.repository.CartJpaRepository;

/**
 * Adapter implementing the CartRepositoryPort using Spring Data JPA.
 * Constructor-based DI used per project rules.
 */
@Repository
public class CartRepositoryAdapter implements CartRepositoryPort {
    private final CartJpaRepository cartJpaRepository;
    private final CartEntityMapper cartEntityMapper;

    public CartRepositoryAdapter(CartJpaRepository cartJpaRepository, CartEntityMapper cartEntityMapper) {
        this.cartJpaRepository = cartJpaRepository;
        this.cartEntityMapper = cartEntityMapper;
    }

    @Override
    public Optional<Cart> findByMemberId(UUID memberId) {
        return cartJpaRepository.findByMemberId(memberId)
                .map(cartEntityMapper::toDomain);
    }
}
