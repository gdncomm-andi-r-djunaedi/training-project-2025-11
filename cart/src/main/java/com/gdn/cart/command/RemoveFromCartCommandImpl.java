package com.gdn.cart.command;

import com.gdn.cart.command.commandInterface.RemoveFromCartCommand;
import com.gdn.cart.command.model.RemoveFromCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartItemResponse;
import com.gdn.cart.controller.webmodel.response.CartResponse;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.exception.DataNotFoundException;
import com.gdn.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RemoveFromCartCommandImpl implements RemoveFromCartCommand {

  private final CartRepository cartRepository;

  @Override
  public CartResponse execute(RemoveFromCartCommandRequest request) {
    Cart cart = cartRepository.findByMemberId(request.getMemberId())
        .orElseThrow(DataNotFoundException::new);

    boolean removed = cart.getItems()
        .removeIf(item -> item.getProductId().equals(request.getProductId()));

    if (!removed) {
      throw new DataNotFoundException();
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

