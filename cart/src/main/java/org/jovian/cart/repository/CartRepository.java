package org.jovian.cart.repository;

import org.jovian.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, Long> {
}
