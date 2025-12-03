package com.marketplace.cart.command;

import com.marketplace.cart.model.AddItemRequest;
import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AddItemToCartCommand implements Command<Cart, AddItemRequest> {

  private final CartRepository cartRepository;

  @Override
  public Cart execute(AddItemRequest request) {
    Cart cart = cartRepository.findById(request.getCustomerId()).orElseGet(() -> {
      Cart c = new Cart();
      c.setCustomerId(request.getCustomerId());
      return c;
    });

    CartItem item = request.getItem();
    cart.getItems()
        .stream()
        .filter(i -> i.getProductId().equals(item.getProductId()))
        .findFirst()
        .ifPresentOrElse(existingItem -> existingItem.setQuantity(
            existingItem.getQuantity() + item.getQuantity()), () -> cart.getItems().add(item));

    return cartRepository.save(cart);
  }
}

