package com.marketplace.cart.service.impl;

import com.marketplace.cart.client.ProductServiceClient;
import com.marketplace.cart.client.dto.ProductResponse;
import com.marketplace.cart.dto.AddItemRequest;
import com.marketplace.cart.dto.CartItemResponse;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.entity.Cart;
import com.marketplace.cart.entity.CartItem;
import com.marketplace.cart.exception.CartNotFoundException;
import com.marketplace.cart.repository.CartRepository;
import com.marketplace.cart.service.CartService;
import com.marketplace.cart.util.ApiResponse;
import com.marketplace.product.exception.ProductNotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        
        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            try {
                ProductResponse product = fetchProduct(item.getProductId());
                item.setPrice(product.getPrice());
            } catch (Exception ex) {
                iterator.remove();
                log.warn("Product removed from catalog. Removing from cart. productId = {}", item.getProductId());
            }
        }
        calculateTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse addItem(String userId, AddItemRequest request, String action) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        ProductResponse product = fetchProduct(request.getProductId());
        Optional<CartItem> existingItemOptional = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOptional.isPresent()) {
            CartItem existingItem = existingItemOptional.get();
            int newQuantity;

            if ("set".equalsIgnoreCase(action) || action == null) {
                newQuantity = request.getQuantity();
            } else if ("increase".equalsIgnoreCase(action)) {
                newQuantity = existingItem.getQuantity() + request.getQuantity();
            } else if ("decrease".equalsIgnoreCase(action)) {
                newQuantity = existingItem.getQuantity() - request.getQuantity();
            } else {
                newQuantity = request.getQuantity(); // Default to set
            }

            if (newQuantity <= 0) {
                cart.getItems().remove(existingItem);
                log.info("Item removed from cart (quantity {} to 0) for userId: {}, productId: {}", action, userId, request.getProductId());
            } else {
                existingItem.setQuantity(newQuantity);
                existingItem.setPrice(product.getPrice());
                log.info("Item quantity {} for userId: {}, productId: {}, newQuantity: {}", action, userId, request.getProductId(), newQuantity);
            }
        } else {
            if ("decrease".equalsIgnoreCase(action)) {
                log.warn("Attempted to decrease quantity for non-existent item. userId: {}, productId: {}", userId, request.getProductId());
                return mapToResponse(cart);
            }
            cart.getItems().add(CartItem.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build());
            log.info("Item added to cart for userId: {}, productId: {}", userId, request.getProductId());
        }

        calculateTotal(cart);
        return mapToResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse removeItem(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for userId: " + userId));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        calculateTotal(cart);
        log.info("Item removed from cart for userId: {}, productId: {}", userId, productId);
        return mapToResponse(cartRepository.save(cart));
    }

    private Cart createNewCart(String userId) {
        log.info("Creating new cart for userId: {}", userId);
        return cartRepository.save(Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .total(BigDecimal.ZERO)
                .build());
    }

    private void calculateTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal(total);
    }

    private ProductResponse fetchProduct(String productId) {
        try {
            ApiResponse<ProductResponse> response = productServiceClient.getProduct(productId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            throw new ProductNotFoundException(productId);
        } catch (FeignException e) {
            log.error("Error fetching product {} from Product Service: {}", productId, e.getMessage());
            throw new ProductNotFoundException(productId);
        }
    }



    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemResponses)
                .total(cart.getTotal())
                .build();
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        BeanUtils.copyProperties(item, response);
        return response;
    }
}
