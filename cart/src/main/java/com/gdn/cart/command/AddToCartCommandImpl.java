package com.gdn.cart.command;

import com.gdn.cart.client.ProductClient;
import com.gdn.cart.client.model.ProductResponse;
import com.gdn.cart.command.commandInterface.AddToCartCommand;
import com.gdn.cart.command.model.AddToCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartItemResponse;
import com.gdn.cart.controller.webmodel.response.CartResponse;
import com.gdn.cart.entity.Cart;
import com.gdn.cart.entity.CartItem;
import com.gdn.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddToCartCommandImpl implements AddToCartCommand {

  private final CartRepository cartRepository;
  private final ProductClient productClient;

  @Override
  public CartResponse execute(AddToCartCommandRequest request) {
    // Fetch product details from Product Service
    ProductResponse product = productClient.getProductById(request.getProductId());

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
      // Update quantity and price (price might have changed)
      existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
      existingItem.get().setPrice(product.getPrice());
      existingItem.get().setProductName(product.getName());
    } else {
      // Add new item with product details from Product Service
      CartItem newItem = CartItem.builder()
          .productId(product.getId())
          .productName(product.getName())
          .price(product.getPrice())
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

