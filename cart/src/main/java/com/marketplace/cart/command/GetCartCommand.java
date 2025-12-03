package com.marketplace.cart.command;

import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.GetCartRequest;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetCartCommand implements Command<Cart, GetCartRequest> {

  private final CartRepository cartRepository;

  @Override
  public Cart execute(GetCartRequest request) {
    return cartRepository.findById(request.getCustomerId()).orElseGet(() -> {
      Cart cart = new Cart();
      cart.setCustomerId(request.getCustomerId());
      return cartRepository.save(cart);
    });
  }
}
