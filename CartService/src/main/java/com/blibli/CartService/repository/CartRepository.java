package com.blibli.CartService.repository;

import com.blibli.CartService.entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<CartEntity, String> {

    Optional<CartEntity> findByUserId(String userId);
}
