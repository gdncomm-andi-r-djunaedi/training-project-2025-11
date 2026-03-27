package com.ecom.cart.Repository;

import com.ecom.cart.Entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepo extends MongoRepository<Cart,String> {

    Optional<Cart> findByUserId(String userId);

}
