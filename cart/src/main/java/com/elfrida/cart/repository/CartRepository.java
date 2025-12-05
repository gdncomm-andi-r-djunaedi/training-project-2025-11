package com.elfrida.cart.repository;

import com.elfrida.cart.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {

    Optional<Cart> findByMemberId(String memberId);
}


