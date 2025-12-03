package com.marketplace.cart.command;

import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.RemoveItemRequest;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RemoveItemFromCartCommand implements Command<Cart, RemoveItemRequest> {

  private final CartRepository cartRepository;

  @Override
  public Cart execute(RemoveItemRequest request) {
    Cart cart = cartRepository.findById(request.getCustomerId()).orElseGet(() -> {
      Cart c = new Cart();
      c.setCustomerId(request.getCustomerId());
      return c;
    });

    if (request.getProductId() != null) {
      cart.getItems().removeIf(i -> i.getProductId().equals(request.getProductId()));
    } else {
      cart.getItems().clear();
    }

    return cartRepository.save(cart);
  }
}
