package com.gdn.training.cart.infrasturcture.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gdn.training.cart.infrasturcture.entity.CartItemEntity;

@Repository
public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {

}
