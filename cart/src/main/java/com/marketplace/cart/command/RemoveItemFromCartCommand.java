package com.marketplace.cart.command;

import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.RemoveItemRequest;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
public class RemoveItemFromCartCommand implements Command<Cart, RemoveItemRequest> {

  private final CartRepository cartRepository;

  @Override
  public Mono<Cart> execute(RemoveItemRequest request) {
    return Mono.fromCallable(() -> {

      Cart cart = cartRepository.findByCustomerId(request.getCustomerId())
          .orElseThrow(() -> new RuntimeException("Cart not found"));

      String productId = request.getProductId();

      if (productId != null && !productId.isBlank()) {
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
      } else {
        cart.getItems().clear();
      }

      return cartRepository.save(cart);

    }).subscribeOn(Schedulers.boundedElastic());
  }


}
