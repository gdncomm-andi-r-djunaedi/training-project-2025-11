package com.example.marketplace.cart.service;

import com.example.marketplace.cart.client.MemberClient;
import com.example.marketplace.cart.client.ProductClient;
import com.example.marketplace.cart.entity.CartItem;
import com.example.marketplace.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final MemberClient memberClient;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository,
                       MemberClient memberClient,
                       ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.memberClient = memberClient;
        this.productClient = productClient;
    }

    public CartItem addToCart(String userId, String productId, int quantity) {

        Boolean exists = memberClient.exists(userId);
        if (exists == null || !exists) {
            throw new RuntimeException("Member does not exist: " + userId);
        }

        Object product = productClient.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product does not exist: " + productId);
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);

        return cartRepository.save(item);
    }

    public List<CartItem> viewCart(String userId) {
        return cartRepository.findByUserId(userId);
    }

    public void deleteItem(String userId, String productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
