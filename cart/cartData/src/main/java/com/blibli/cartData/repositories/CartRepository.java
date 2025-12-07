package com.blibli.cartData.repositories;

import com.blibli.cartData.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart,String> {
    Optional<Cart> findByMemberId(String memberId);

    Page<Cart> findAllByMemberId(String memberId, Pageable pageable);
}
