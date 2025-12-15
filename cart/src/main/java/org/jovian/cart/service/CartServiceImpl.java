package org.jovian.cart.service;

import lombok.RequiredArgsConstructor;
import org.jovian.cart.entity.Cart;
import org.jovian.cart.entity.CartItem;
import org.jovian.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    public final CartRepository cartRepository;

    public Cart getOrCreateCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseGet(() -> Cart.builder()
                        .id(cartId)
                        .productIds(new ArrayList<>())
                        .build());
    }

    //No validation on existence of product yet
    @Transactional
    @Override
    public void addProduct(Long cartId, String productId, int quantity) {
        Cart cart = getOrCreateCart(cartId);

        CartItem existingItem = cart.getProductIds().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.getProductIds().add(CartItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build());
        }
    }

    @Transactional
    @Override
    public void removeProduct(Long cartId, String productId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem existingItem = cart.getProductIds().stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        if (existingItem.getQuantity() <= quantity) {
            cart.getProductIds().remove(existingItem);
        } else {
            existingItem.setQuantity(existingItem.getQuantity() - quantity);
        }

        cartRepository.save(cart);
    }

    @Override
    public List<CartItem> getCartProducts(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return cart.getProductIds();
    }

    @Transactional
    @Override
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getProductIds().clear();
        cartRepository.save(cart);
    }
}
