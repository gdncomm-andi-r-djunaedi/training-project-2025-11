package com.gdn.faurihakim.cart.command.impl;

import com.gdn.faurihakim.Cart;
import com.gdn.faurihakim.CartItem;
import com.gdn.faurihakim.CartRepository;
import com.gdn.faurihakim.cart.command.UpdateCartItemCommand;
import com.gdn.faurihakim.cart.command.model.UpdateCartItemCommandRequest;
import com.gdn.faurihakim.cart.web.model.CartNotFoundException;
import com.gdn.faurihakim.cart.web.model.ProductNotFoundException;
import com.gdn.faurihakim.cart.web.model.response.CartItemResponse;
import com.gdn.faurihakim.cart.web.model.response.UpdateCartItemWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UpdateCartItemCommandImpl implements UpdateCartItemCommand {
        @Autowired
        private CartRepository cartRepository;

        @Override
        @Transactional
        public UpdateCartItemWebResponse execute(UpdateCartItemCommandRequest request) {
                Cart cart = cartRepository.findByCartId(request.getCartId())
                                .orElseThrow(() -> new CartNotFoundException("Cart not found: " + request.getCartId()));

                // Find and update the cart item
                CartItem itemToUpdate = cart.getItems().stream()
                                .filter(item -> item.getProductId().equals(request.getProductId()))
                                .findFirst()
                                .orElseThrow(() -> new ProductNotFoundException(
                                                "Product not found in cart: " + request.getProductId()));

                itemToUpdate.setQuantity(request.getQuantity());
                itemToUpdate.calculateSubtotal();

                // Recalculate total amount
                cart.calculateTotalAmount();

                // Save cart
                Cart savedCart = cartRepository.save(cart);

                // Build response
                List<CartItemResponse> itemResponses = savedCart.getItems().stream()
                                .map(item -> CartItemResponse.builder()
                                                .productId(item.getProductId())
                                                .productName(item.getProductName())
                                                .price(item.getPrice())
                                                .quantity(item.getQuantity())
                                                .subtotal(item.getSubtotal())
                                                .build())
                                .collect(Collectors.toList());

                return UpdateCartItemWebResponse.builder()
                                .cartId(savedCart.getCartId())
                                .memberId(savedCart.getMemberId())
                                .totalAmount(savedCart.getTotalAmount())
                                .items(itemResponses)
                                .version(savedCart.getVersion())
                                .createdDate(savedCart.getCreatedDate())
                                .createdBy(savedCart.getCreatedBy())
                                .lastModifiedDate(savedCart.getLastModifiedDate())
                                .lastModifiedBy(savedCart.getLastModifiedBy())
                                .build();
        }
}
