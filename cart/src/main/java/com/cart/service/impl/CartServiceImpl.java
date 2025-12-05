package com.cart.service.impl;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;
import com.cart.repository.CartItemRepository;
import com.cart.repository.CartRepository;
import com.cart.service.CartService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Cart getOrCreateCart(UUID customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            cart = new Cart();
            cart.setCustomerId(customerId);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    private void updateCartTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        BigDecimal total = items.stream()
                .map(i -> i.getPriceEach().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(total);
        cartRepository.save(cart);
    }

    @Override
    public List<CartItem> getCartItems(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    public CartItem addItem(UUID customerId, AddItemRequest request) {
        Cart cart = getOrCreateCart(customerId);

        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            CartItem savedItem = cartItemRepository.save(existing);
            updateCartTotal(cart);
            cartItemRepository.flush();
            entityManager.refresh(savedItem);
            return savedItem;
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setPriceEach(request.getPriceEach());
        item.setProductName(request.getProductName());
        cart.getItems().add(item);

        CartItem savedItem = cartItemRepository.save(item);
        updateCartTotal(cart);
        cartItemRepository.flush();
        entityManager.refresh(savedItem);
        return savedItem;
    }

    @Override
    public CartItem updateItemQuantity(UUID customerId, UpdateItemQuantityRequest request) {
        Cart cart = getOrCreateCart(customerId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());
        if (item == null) throw new RuntimeException("Item not found in cart");

        item.setQuantity(request.getQuantity());
        CartItem savedItem = cartItemRepository.save(item);

        updateCartTotal(cart);
        cartItemRepository.flush();
        entityManager.refresh(savedItem);

        return savedItem;
    }

    @Override
    public Cart removeItem(UUID customerId, UUID productId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (item != null) {
            cartItemRepository.delete(item);
            updateCartTotal(cart);
        }
        return cart;
    }

    @Override
    @Transactional
    public void clearCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteAllByCartId(cart.getId());
    }

}

