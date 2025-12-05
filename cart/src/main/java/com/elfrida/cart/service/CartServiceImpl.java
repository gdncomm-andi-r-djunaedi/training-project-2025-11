package com.elfrida.cart.service;

import com.elfrida.cart.model.Cart;
import com.elfrida.cart.model.CartItem;
import com.elfrida.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    public Cart addToCart(String memberId, String productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setMemberId(memberId);
                    c.setCreatedAt(Instant.now());
                    return c;
                });

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> productId.equals(i.getProductId()))
                .findFirst();

        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            int newQty = item.getQuantity() + quantity;
            item.setQuantity(newQty);
            BigDecimal unitPrice = item.getTotalPrice()
                    .divide(BigDecimal.valueOf(item.getQuantity() - quantity <= 0 ? 1 : (item.getQuantity() - quantity)));
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(newQty)));
        } else {
            item = new CartItem();
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setTotalPrice(BigDecimal.ZERO);
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }

    @Override
    public Cart getCart(String memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setMemberId(memberId);
                    c.setCreatedAt(Instant.now());
                    c.setUpdatedAt(Instant.now());
                    return c;
                });
    }

    @Override
    public Cart removeItem(String memberId, String productId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> productId.equals(item.getProductId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart");
        }

        cart.setUpdatedAt(Instant.now());
        return cartRepository.save(cart);
    }
}


