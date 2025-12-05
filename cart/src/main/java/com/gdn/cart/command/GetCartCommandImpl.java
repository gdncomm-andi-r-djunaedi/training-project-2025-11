package com.gdn.cart.command;

import com.gdn.cart.command.commandInterface.GetCartCommand;
import com.gdn.cart.command.model.GetCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartItemResponse;
import com.gdn.cart.controller.webmodel.response.CartResponse;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GetCartCommandImpl implements GetCartCommand {

  private final CartRepository cartRepository;

  @Override
  public CartResponse execute(GetCartCommandRequest request) {
    Cart cart = cartRepository.findByMemberId(request.getMemberId())
        .orElseGet(() -> Cart.builder()
            .memberId(request.getMemberId())
            .items(new ArrayList<>())
            .build());

    return toResponse(cart);
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

