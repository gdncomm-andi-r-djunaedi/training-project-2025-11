package com.kailash.cart.repository;

import com.kailash.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart,String> {
    Optional<Cart> findByMemberId(String memberId);

}
