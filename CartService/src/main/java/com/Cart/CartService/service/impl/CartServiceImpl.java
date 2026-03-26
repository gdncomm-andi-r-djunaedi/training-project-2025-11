package com.Cart.CartService.service.impl;

import com.Cart.CartService.dto.*;
import com.Cart.CartService.entity.Cart;
import com.Cart.CartService.entity.CartItem;
import com.Cart.CartService.exception.ProductServiceExceptions;
import com.Cart.CartService.repository.CartRepository;
import com.Cart.CartService.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductClient productClient;

//    @Override
//    public CartResponseDTO addItem(String memberId, AddItemRequestDTO requestDTO) {
//        Cart cart = cartRepository.findByMemberId(memberId);
//
//        if (cart == null) {
//            cart = new Cart();
//            cart.setCartId(UUID.randomUUID().toString());
//            cart.setMemberId(memberId);
//            cart.setItems(new ArrayList<>());
//        }
//
//        CartItem newItem = new CartItem(
//                UUID.randomUUID().toString(),
//                requestDTO.getItemName(),
//                requestDTO.getItemPrice(),
//                requestDTO.getItemDescription()
//        );
//
//        cart.getItems().add(newItem);
//        Cart saved = cartRepository.save(cart);
//
//        // convert entity -> DTO
//        return new CartResponseDTO(
//                saved.getCartId(),
//                saved.getMemberId(),
//                saved.getItems().stream()
//                        .map(item -> new CartItemResponseDTO(
//                                item.getItemId(),
//                                item.getItemName(),
//                                item.getItemPrice(),
//                                item.getItemDescription()
//                        )).collect(Collectors.toList())
//        );
//    }
//
//    @Override
//    public ApiErrorResponse getCartItems(String memberId) {
//        Cart cart = cartRepository.findByMemberId(memberId);
//
//        if (cart == null || cart.getItems() == null) {
//            return new ApiErrorResponse(Collections.emptyList(), "Cart is Empty");
//        }
//
//        List<CartItemResponseDTO> items = cart.getItems()
//                .stream()
//                .map(i -> new CartItemResponseDTO(
//                        i.getItemId(),
//                        i.getItemName(),
//                        i.getItemPrice(),
//                        i.getItemDescription()
//                ))
//                .collect(Collectors.toList());
//
//        return new ApiErrorResponse(items, "Cart items retrieved successfully");
//    }

    @Override
    public boolean deleteProductFromCart(String memberId, String itemId) {
//        Cart cart = cartRepository.findByMemberId(itemIdOfProductAddedInCart);
//        if (cart == null) {
//            throw new CartServiceExceptions("Cart not found for member: " + memberId, HttpStatus.NOT_FOUND);
//        }
//
//        boolean itemRemoved = cart.getItems().removeIf(item -> item.getItemId().equals(itemIdOfProductAddedInCart));
//
//        if (!itemRemoved) {
//            throw new CartServiceExceptions("Item with ID " + itemIdOfProductAddedInCart + " not found in the cart.", HttpStatus.NOT_FOUND);
//        }
//        cartRepository.save(cart);
        try {
            // 1. Validate inputs
            if (memberId == null || memberId.trim().isEmpty()) {
                log.error("deleteItemFromCart failed: memberId is null or empty");
                return false;
            }

            if (itemId == null || itemId.trim().isEmpty()) {
                log.error("deleteItemFromCart failed: itemId is null or empty");
                return false;
            }

            // 2. Fetch cart
            Cart cart = cartRepository.findByMemberId(memberId);

            if (cart == null) {
                log.warn("Cart not found for memberId: {}", memberId);
                return false;
            }

            // 3. Try removing the item
            boolean removed = cart.getItems().removeIf(item -> itemId.equals(item.getItemId()));

            if (!removed) {
                log.warn("Item not found in cart. itemId: {}, memberId: {}", itemId, memberId);
                return false;
            }

            // 4. Save updated cart
            cartRepository.save(cart);
            log.info("Item successfully removed. itemId: {}, memberId: {}", itemId, memberId);

            return true;

        } catch (Exception ex) {
            log.error("Unexpected error while deleting item from cart: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public CartResponseDTO addItem(String memberId, AddItemRequestDTO requestDTO) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new ProductServiceExceptions("Member ID is required", HttpStatus.BAD_REQUEST);
        }
        if (requestDTO == null || requestDTO.getProductId() == null || requestDTO.getProductId().trim().isEmpty()) {
            throw new ProductServiceExceptions("Product ID is required", HttpStatus.BAD_REQUEST);
        }
        if (requestDTO.getQuantity() <= 0) {
            throw new ProductServiceExceptions("Quantity must be at least 1", HttpStatus.BAD_REQUEST);
        }

        ProductResponseDTO product = productClient.getProductById(requestDTO.getProductId());
        if (product == null) {
            throw new ProductServiceExceptions("Product not found with id: " + requestDTO.getProductId(), HttpStatus.NOT_FOUND);
        }

        Cart cart = cartRepository.findByMemberId(memberId);
        if (cart == null) {
            cart = new Cart();
            cart.setCartId(UUID.randomUUID().toString());
            cart.setMemberId(memberId);
            cart.setItems(new ArrayList<>());
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(requestDTO.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + requestDTO.getQuantity());
        } else {
            CartItem newItem = new CartItem(
                    UUID.randomUUID().toString(),
                    requestDTO.getProductId(),
                    requestDTO.getQuantity()
            );
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);

        return convertCartToResponse(saved);
    }

    @Override
    public CartResponseDTO getCartByMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new ProductServiceExceptions("Member ID is required", HttpStatus.BAD_REQUEST);
        }

        Cart cart = cartRepository.findByMemberId(memberId);
        if (cart == null) {
            CartResponseDTO empty = new CartResponseDTO(UUID.randomUUID().toString(), memberId, Collections.emptyList());
            return empty;
        }

        return convertCartToResponse(cart);
    }

    private CartResponseDTO convertCartToResponse(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream().map(item -> {
            ProductResponseDTO product;
            try {
                product = productClient.getProductById(item.getProductId());
            }
            catch (ProductServiceExceptions ex) {
                product = null;
            }

            return new CartItemResponseDTO(
                    item.getItemId(),
                    item.getProductId(),
                    product != null ? product.getProductName() : null,
                    product != null ? product.getPrice() : null,
                    product != null ? product.getProductDescription() : null,
                    product != null ? product.getCategory() : null,
                    item.getQuantity()
            );
        }).collect(Collectors.toList());

        return new CartResponseDTO(cart.getCartId(), cart.getMemberId(), items);
    }
}
