package com.gdn.marketplace.cart.repository;

import com.gdn.marketplace.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUsername(String username);
}
