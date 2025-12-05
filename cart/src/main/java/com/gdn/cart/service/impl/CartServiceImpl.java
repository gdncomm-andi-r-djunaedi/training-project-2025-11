package com.gdn.cart.service.impl;

import com.gdn.cart.client.ProductClient;
import com.gdn.cart.dto.request.AddCartItemRequestDTO;
import com.gdn.cart.dto.response.ApiResponse;
import com.gdn.cart.dto.request.CartDTO;
import com.gdn.cart.dto.request.CartItemDTO;
import com.gdn.cart.dto.response.ProductDTO;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.entity.CartItem;
import com.gdn.cart.exception.CartException;
import com.gdn.cart.exception.CartNotFoundException;
import com.gdn.cart.exception.ProductNotFoundException;
import com.gdn.cart.respository.CartRepository;
import com.gdn.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }


    private Cart getOrCreateCart(String memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    log.info("Creating new cart for memberId={}", memberId);
                    Cart cart = new Cart();
                    cart.setMemberId(memberId);
                    return cartRepository.save(cart);
                });
    }

    private CartDTO toDto(Cart cart) {
        CartDTO dto = new CartDTO();
        BeanUtils.copyProperties(cart, dto);

        if (cart.getItems() != null) {
            dto.setItems(
                    cart.getItems().stream()
                            .map(item -> {
                                CartItemDTO itemDto = new CartItemDTO();
                                BeanUtils.copyProperties(item, itemDto);
                                return itemDto;
                            })
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }


    @Override
    public CartDTO addItem(String memberId, AddCartItemRequestDTO request) {
        log.info("Adding item to cart, memberId={}, request={}", memberId, request);

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new CartException("Quantity must be greater than zero");
        }

        ApiResponse<ProductDTO> response = productClient.getProductById(request.getProductId());
        if (response == null || !response.isSuccess()) {
            throw new ProductNotFoundException(request.getProductId());
        }

        ProductDTO product = response.getData();
        if (product == null || String.valueOf(product.getPrice()) == null) {
            throw new ProductNotFoundException(request.getProductId());
        }

        Cart cart = getOrCreateCart(memberId);
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setProductId(product.getProductId());
            item.setProductName(product.getProductName());
            item.setPrice(product.getPrice());
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
        }

        cart.recalculateTotal();
        Cart saved = cartRepository.save(cart);

        log.info("Item added to cart successfully, memberId={}, total={}", memberId, saved.getTotalAmount());
        return toDto(saved);
    }

    @Override
    public CartDTO getCart(String memberId) {
        log.info("Fetching cart for memberId={}", memberId);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException(memberId));

        return toDto(cart);
    }


    @Override
    public void deleteItem(String memberId, String productId) {
        log.info("Deleting item from cart, memberId={}, productId={}", memberId, productId);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException(memberId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartException("Cart is empty for memberId: " + memberId);
        }

        boolean removed = cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        if (!removed) {
            throw new CartException("Item not found in cart for productId: " + productId);
        }

        if (cart.getItems().isEmpty()) {
            clearCart(memberId);
        } else {
            cart.recalculateTotal();
            cartRepository.save(cart);
        }

        log.info("Item removed from cart, memberId={}, productId={}", memberId, productId);
    }

    @Override
    public void clearCart(String memberId) {
        log.info("Clearing cart for memberId={}", memberId);

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CartNotFoundException(memberId));

        cartRepository.delete(cart);

        log.info("Cart cleared for memberId={}", memberId);
    }
}
