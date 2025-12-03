package com.blibli.gdn.cartService.repository;

import com.blibli.gdn.cartService.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByMemberId(String memberId);
    void deleteByMemberId(String memberId);
}
