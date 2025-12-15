package com.blibli.cart.repository;

import com.blibli.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<Cart,String> {
    Cart findByUserEmail(String customerEmail);

    Cart deleteByUserEmail(String customerEmail);
}
