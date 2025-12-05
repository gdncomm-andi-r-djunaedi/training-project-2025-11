package com.training.cart.service;

import com.training.cart.dto.AddToCartRequest;
import com.training.cart.dto.CartItemResponse;
import com.training.cart.dto.CartResponse;
import com.training.cart.dto.ProductResponse;
import com.training.cart.entity.Cart;
import com.training.cart.entity.CartItem;
import com.training.cart.repository.CartItemRepository;
import com.training.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Transactional
    public CartResponse addToCart(String customerEmail, AddToCartRequest request) {
        ProductResponse product = getProductById(request.getProductId());

        Cart cart = cartRepository.findByCustomerEmail(customerEmail)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .customerEmail(customerEmail)
                            .build();
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(cart, request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCart(customerEmail);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String customerEmail) {
        Cart cart = cartRepository.findByCustomerEmail(customerEmail)
                .orElseGet(() -> Cart.builder()
                        .customerEmail(customerEmail)
                        .build());

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = itemResponses.stream()
                .map(CartItemResponse::getQuantity)
                .reduce(0, Integer::sum);

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerEmail(cart.getCustomerEmail())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    @Transactional
    public void removeFromCart(String customerEmail, Long cartItemId) {
        Cart cart = cartRepository.findByCustomerEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item does not belong to your cart");
        }

        cartItemRepository.delete(item);
    }

    private ProductResponse getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            return restTemplate.getForObject(url, ProductResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable");
        }
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProductPrice().multiply(new BigDecimal(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productPrice(item.getProductPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
