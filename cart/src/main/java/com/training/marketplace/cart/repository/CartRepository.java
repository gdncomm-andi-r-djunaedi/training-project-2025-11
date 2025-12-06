package com.training.marketplace.cart.repository;

import java.util.Optional;
import java.util.UUID;

import com.training.marketplace.cart.entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<CartEntity, UUID> {
    Optional<CartEntity> findByUserId(String userId);
}