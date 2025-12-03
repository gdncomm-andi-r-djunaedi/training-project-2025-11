package com.cart.repository;

import com.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {

    // find cart by user id
    Cart findByCustomerId(UUID customerId);
}