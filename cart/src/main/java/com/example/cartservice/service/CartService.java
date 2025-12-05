package com.example.cartservice.service;

import com.example.cartservice.client.ProductClient;
import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.CartDTO;
import com.example.cartservice.dto.CartItemDTO;
import com.example.cartservice.dto.ProductDTO;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.entity.CartItem;
import com.example.cartservice.repository.CartRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository repository;
    private final ProductClient productClient;

    public CartDTO getCart(Long userId) {
        Cart cart = repository.findById(userId)
                .orElse(new Cart(userId, new ArrayList<>()));

        // Enrich cart items with product details
        List<CartItemDTO> enrichedItems = cart.getItems().stream()
                .map(this::enrichCartItem)
                .toList();

        return new CartDTO(userId, enrichedItems);
    }

    private CartItemDTO enrichCartItem(CartItem item) {
        try {
            ProductDTO product = productClient.getProduct(item.getProductId());
            return new CartItemDTO(
                    item.getProductId(),
                    item.getQuantity(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice());
        } catch (FeignException.NotFound e) {
            log.warn("Product not found: {}", item.getProductId());
            return new CartItemDTO(
                    item.getProductId(),
                    item.getQuantity(),
                    "Product Not Available",
                    "This product is no longer available",
                    null);
        } catch (Exception e) {
            log.error("Error fetching product details for: {}", item.getProductId(), e);
            return new CartItemDTO(
                    item.getProductId(),
                    item.getQuantity(),
                    "Product details unavailable",
                    "Unable to load product information",
                    null);
        }
    }

    public Cart addToCart(Long userId, AddToCartRequest request) {
        Cart cart = repository.findById(userId)
                .orElse(new Cart(userId, new ArrayList<>()));

        // Create CartItem with only productId and quantity
        CartItem item = new CartItem(request.getProductId(), request.getQuantity());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            cart.getItems().add(item);
        }
        return repository.save(cart);
    }

    public Cart removeFromCart(Long userId, String productId) {
        Cart cart = repository.findById(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return repository.save(cart);
    }
}
