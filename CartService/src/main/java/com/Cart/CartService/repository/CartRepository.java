package com.Cart.CartService.repository;

import com.Cart.CartService.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends MongoRepository<Cart,String> {
    Cart findByMemberId(String memberId);
}
