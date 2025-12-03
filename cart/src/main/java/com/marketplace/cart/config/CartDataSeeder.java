package com.marketplace.cart.config;

import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.repository.CartRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Profile("dev")
@Component
public class CartDataSeeder implements CommandLineRunner {

  private final CartRepository cartRepository;
  private final Random random = new Random();

  public CartDataSeeder(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    // Clear existing data
    cartRepository.deleteAll();

    int numberOfCustomers = 3;
    int numberOfProducts = 5;

    List<String> productIds = new ArrayList<>();
    for (int i = 1; i <= numberOfProducts; i++) {
      productIds.add(String.format("product%03d", i));
    }

    for (int i = 1; i <= numberOfCustomers; i++) {
      Cart cart = new Cart();
      cart.setCustomerId(String.format("customer%04d", i));

      int itemsCount = random.nextInt(5) + 1; // 1–5 items per cart
      List<CartItem> items = new ArrayList<>();

      for (int j = 0; j < itemsCount; j++) {
        String productId = productIds.get(random.nextInt(productIds.size()));
        int quantity = random.nextInt(5) + 1; // quantity 1–5
        items.add(new CartItem(productId, quantity));
      }

      cart.setItems(items);
      cartRepository.save(cart);
    }

    System.out.println("Cart data seeding completed: " + numberOfCustomers + " carts created.");
  }
}

