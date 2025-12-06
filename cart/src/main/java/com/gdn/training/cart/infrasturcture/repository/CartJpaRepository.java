package com.gdn.training.cart.infrasturcture.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gdn.training.cart.infrasturcture.entity.CartEntity;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {
    Optional<CartEntity> findByMemberId(UUID memberId);
}
