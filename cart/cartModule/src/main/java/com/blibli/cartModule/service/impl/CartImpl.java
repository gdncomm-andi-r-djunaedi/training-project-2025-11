package com.blibli.cartModule.service.impl;

import com.blibli.cartModule.client.ProductServiceClient;
import com.blibli.cartModule.dto.AddItemRequestDto;
import com.blibli.cartModule.dto.CartItemDto;
import com.blibli.cartModule.dto.CartResponseDto;
import com.blibli.cartModule.dto.RemoveItemDto;
import com.blibli.cartModule.entity.Cart;
import com.blibli.cartModule.repository.CartRepository;
import com.blibli.cartModule.service.CartService;
import com.blibli.productModule.dto.ApiResponse;
import com.blibli.productModule.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public CartResponseDto addItem(Long memberId, AddItemRequestDto request) {
        log.info("Adding item to cart - memberId: {}, productId: {}, quantity: {}", memberId,
                request.getProductId(), request.getQuantity());
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Invalid quantity provided - memberId: {}, productId: {}, quantity: {}", 
                    memberId, request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        fetchProductDetails(request.getProductId());
        Cart cart = cartRepository.findByMemberId(memberId).orElseGet(() -> createNewCart(memberId));
        Cart.CartItem existingItem =
                cart.getItems().stream().filter(item -> item.getProductId().equals(request.getProductId()))
                        .findFirst().orElse(null);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            Cart.CartItem newItem = new Cart.CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            cart.getItems().add(newItem);
        }
        cart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart successfully - cartId: {}", savedCart.getId());
        return convertToResponse(savedCart);
    }

    @Override
    public CartResponseDto removeItem(Long memberId, RemoveItemDto request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            log.warn("Invalid quantity provided - memberId: {}, productId: {}, quantity: {}",
                    memberId, request.getProductId(), request.getQuantity());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        log.info("Removing item from cart - memberId: {}, productId: {}, quantity: {}", 
                memberId, request.getProductId(), request.getQuantity());
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Cart not found for member: " + memberId));
        Cart.CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart: " + request.getProductId()));
        int newQuantity = existingItem.getQuantity() - request.getQuantity();
        if (newQuantity <= 0) {
            cart.getItems().remove(existingItem);
            log.info("Item removed from cart completely - productId: {}", request.getProductId());
        } else {
            existingItem.setQuantity(newQuantity);
            log.info("Item quantity updated - productId: {}, newQuantity: {}", request.getProductId(), newQuantity);
        }
        cart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(cart);
        log.info("Item removed from cart successfully - cartId: {}", savedCart.getId());
        return convertToResponse(savedCart);
    }

    @Override
    public CartResponseDto getCart(Long memberId) {
        log.info("Get cart for - memberId: {}", memberId);
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(() -> new RuntimeException("Member id not found"));
        return convertToResponse(cart);
    }

    @Override
    public void clearCart(Long memberId) {
        log.info("Clear cart for - memberId: {}", memberId);
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(() -> new RuntimeException("Member id not found"));
        cart.getItems().clear();
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
    }

    private Cart createNewCart(Long memberId) {
        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setItems(new java.util.ArrayList<>());
        return cart;
    }

    private ProductDto fetchProductDetails(String productId) {
        log.info("Fetching product details for productId: {}", productId);
        try {
            ResponseEntity<ApiResponse<ProductDto>> response =
                    productServiceClient.getProductById(productId);
            if (response.getBody() != null && response.getBody().getValue() != null) {
                return response.getBody().getValue();
            }
            throw new RuntimeException("Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product details for productId: {}", productId, e);
            throw new RuntimeException("Failed to fetch product details: " + productId, e);
        }
    }

    private CartResponseDto convertToResponse(Cart cart) {
        CartResponseDto response = new CartResponseDto();
        response.setMemberId(cart.getMemberId());

        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> {
                    try {
                        ProductDto product = fetchProductDetails(item.getProductId());
                        CartItemDto dto = new CartItemDto();
                        dto.setProductId(item.getProductId());
                        dto.setProductName(product.getName());
                        dto.setProductImageUrl(product.getImageUrl());
                        dto.setProductPrice(product.getPrice());
                        dto.setQuantity(item.getQuantity());
                        dto.setItemPrice(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
                        return dto;
                    } catch (Exception e) {
                        log.warn("Product not found in product service - productId: {}, removing from cart response", 
                                item.getProductId(), e);
                        return null; // Return null to filter out later
                    }
                })
                .filter(dto -> dto != null) // Filter out items where product doesn't exist
                .collect(Collectors.toList());

        response.setItems(items);

        BigDecimal totalPrice = items.stream()
                .map(CartItemDto::getItemPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalPrice(totalPrice);

        return response;
    }


}
