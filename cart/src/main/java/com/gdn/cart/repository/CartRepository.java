package com.gdn.cart.repository;

import com.gdn.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

  Optional<Cart> findByMemberId(String memberId);
}

