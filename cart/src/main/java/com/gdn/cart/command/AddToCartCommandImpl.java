package com.gdn.cart.command;

import com.gdn.cart.command.commandInterface.AddToCartCommand;
import com.gdn.cart.command.model.AddToCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartItemResponse;
import com.gdn.cart.controller.webmodel.response.CartResponse;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.entity.CartItem;
import com.gdn.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

  private final CartRepository cartRepository;

  @Override
  public CartResponse execute(AddToCartCommandRequest request) {
    // Find existing cart or create new one
    Cart cart = cartRepository.findByMemberId(request.getMemberId())
        .orElseGet(() -> Cart.builder()
            .memberId(request.getMemberId())
            .items(new ArrayList<>())
            .build());

    // Check if product already exists in cart
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProductId().equals(request.getProductId()))
        .findFirst();

    if (existingItem.isPresent()) {
      // Update quantity
      existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
    } else {
      // Add new item
      CartItem newItem = CartItem.builder()
          .productId(request.getProductId())
          .productName(request.getProductName())
          .price(request.getPrice())
          .quantity(request.getQuantity())
          .build();
      cart.getItems().add(newItem);
    }

    Cart savedCart = cartRepository.save(cart);
    return toResponse(savedCart);
  }

  private CartResponse toResponse(Cart cart) {
    var items = cart.getItems().stream()
        .map(item -> CartItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .price(item.getPrice())
            .quantity(item.getQuantity())
            .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .build())
        .toList();

    BigDecimal totalPrice = items.stream()
        .map(CartItemResponse::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalItems = items.stream()
        .mapToInt(CartItemResponse::getQuantity)
        .sum();

    return CartResponse.builder()
        .cartId(cart.getId())
        .memberId(cart.getMemberId())
        .items(items)
        .totalPrice(totalPrice)
        .totalItems(totalItems)
        .build();
  }
}

