package com.gdn.cart.respository;

import com.gdn.cart.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {

    Optional<Cart> findByMemberId(String memberId);

    void deleteByMemberId(String memberId);
    List<Cart> findByItemsProductId(String productId);

}
