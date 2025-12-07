package com.blibli.cartData.services.impl;

import com.blibli.cartData.client.ProductClient;
import com.blibli.cartData.dto.*;
import com.blibli.cartData.entity.Cart;
import com.blibli.cartData.entity.CartItem;
import com.blibli.cartData.repositories.CartRepository;
import com.blibli.cartData.services.CartService;
import com.blibli.cartData.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductClient productClient;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public Page<CartProductDetailDTO> getAllCartItems(String authToken, Pageable pageable) {
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();
        }
        String memberId = jwtUtil.getUserNameFromToken(authToken);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Cart not found for member: " + memberId));

        List<CartItem> items = cart.getItems();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<CartItem> pagedItems = items.subList(start, end);

        List<CartProductDetailDTO> dtoList = pagedItems.stream()
                .map(this::mapCartItemToProductDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, items.size());
    }

    @Override
    public CartResponseDTO addProductToCart(String authToken, CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();
        }

        String memberId = jwtUtil.getUserNameFromToken(authToken);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setMemberId(memberId);
                    newCart.setItems(new ArrayList<>());
                    return newCart;
                });

        ProductDTO productData = productClient.getProductById(cartItemDTO.getProductId());
        if (productData == null) {
            throw new IllegalArgumentException("Invalid product ID");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(cartItemDTO.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            if (cartItemDTO.getQuantity() > 0) {
                existingItem.get().setQuantity(cartItemDTO.getQuantity());
            }
        } else {
            if (cartItemDTO.getQuantity() > 0) {
                CartItem cartItem = new CartItem(cartItemDTO.getProductId(), cartItemDTO.getQuantity());
                cart.getItems().add(cartItem);
            }
        }

        cartRepository.save(cart);
        return new CartResponseDTO(cartItemDTO.getProductId(), cartItemDTO.getQuantity(), "Product added successfully!");
    }

    @Override
    public void deleteCartItem(String authToken, String productId) {

        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7).trim();
        }
        String memberId = jwtUtil.getUserNameFromToken(authToken);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Cart not found for member :" + memberId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new RuntimeException("ProductId not found in cart");
        }

        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
        } else {
            cartRepository.save(cart);
        }
    }

    public CartProductDetailDTO mapCartItemToProductDTO(CartItem cartItem) {
        ProductDTO product = productClient.getProductById(cartItem.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + cartItem.getProductId());
        }
        return new CartProductDetailDTO(product.getProductId(),
                product.getName(),
                product.getPrice()*cartItem.getQuantity(),
                product.getImageUrl(),
                cartItem.getQuantity());
    }
}
