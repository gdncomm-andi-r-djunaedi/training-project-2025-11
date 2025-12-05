package com.gdn.training.cart_service.repository;

import com.gdn.training.cart_service.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {

    Optional<Cart> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
