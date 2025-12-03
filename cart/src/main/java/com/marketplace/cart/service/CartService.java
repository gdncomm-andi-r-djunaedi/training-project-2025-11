package com.marketplace.cart.service;

import com.marketplace.cart.command.AddItemToCartCommand;
import com.marketplace.cart.command.GetCartCommand;
import com.marketplace.cart.command.RemoveItemFromCartCommand;
import com.marketplace.cart.model.*;
import org.springframework.stereotype.Service;

@Service
public class CartService {

  private final AddItemToCartCommand addItemCommand;
  private final RemoveItemFromCartCommand removeItemCommand;
  private final GetCartCommand getCartCommand;

  public CartService(AddItemToCartCommand addItemCommand,
      RemoveItemFromCartCommand removeItemCommand,
      GetCartCommand getCartCommand) {
    this.addItemCommand = addItemCommand;
    this.removeItemCommand = removeItemCommand;
    this.getCartCommand = getCartCommand;
  }

  public Cart getCart(String customerId) {
    return getCartCommand.execute(new GetCartRequest(customerId));
  }

  public Cart addItem(String customerId, CartItem item) {
    return addItemCommand.execute(new AddItemRequest(customerId, item));
  }

  public Cart removeItem(String customerId, String productId) {
    return removeItemCommand.execute(new RemoveItemRequest(customerId, productId));
  }

  public void clearCart(String customerId) {
    removeItemCommand.execute(new RemoveItemRequest(customerId, null));
  }
}
