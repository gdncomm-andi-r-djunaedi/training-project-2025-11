package com.example.cartservice.repository;

import com.example.cartservice.model.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Page<Cart> findByMemberId(String memberId, Pageable pageable);

    Optional<Cart> findByMemberIdAndProductId(String memberId, String productId);

}