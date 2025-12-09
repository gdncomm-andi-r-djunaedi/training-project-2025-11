package com.gdn.project.waroenk.cart.fixture;

import com.gdn.project.waroenk.cart.entity.AddressSnapshot;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.CartItem;
import com.gdn.project.waroenk.cart.entity.Checkout;
import com.gdn.project.waroenk.cart.entity.CheckoutItem;
import net.datafaker.Faker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory class for creating test data using DataFaker.
 * Provides consistent mock data for unit and integration tests.
 */
public class TestDataFactory {

  private static final Faker faker = new Faker();

  // ============================================================
  // Cart and CartItem
  // ============================================================

  /**
   * Creates a Cart entity with random data and empty items.
   */
  public static Cart createEmptyCart() {
    return Cart.builder()
        .id(UUID.randomUUID().toString())
        .userId(UUID.randomUUID().toString())
        .items(new ArrayList<>())
        .currency("IDR")
        .version(1)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * Creates a Cart entity with specific user ID.
   */
  public static Cart createCartForUser(String userId) {
    return Cart.builder()
        .id(UUID.randomUUID().toString())
        .userId(userId)
        .items(new ArrayList<>())
        .currency("IDR")
        .version(1)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * Creates a Cart entity with sample items.
   */
  public static Cart createCartWithItems(String userId, int itemCount) {
    List<CartItem> items = new ArrayList<>();
    for (int i = 0; i < itemCount; i++) {
      items.add(createCartItem());
    }
    return Cart.builder()
        .id(UUID.randomUUID().toString())
        .userId(userId)
        .items(items)
        .currency("IDR")
        .version(1)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /**
   * Creates a CartItem with random data.
   */
  public static CartItem createCartItem() {
    String sku = "SKU-" + faker.number().digits(6);
    String subSku = sku + "-VAR-" + faker.number().digits(3);
    return CartItem.builder()
        .sku(sku)
        .subSku(subSku)
        .title(faker.commerce().productName())
        .priceSnapshot((long) faker.number().numberBetween(10000, 1000000))
        .quantity(faker.number().numberBetween(1, 5))
        .availableStockSnapshot(faker.number().numberBetween(10, 100))
        .imageUrl("https://example.com/images/" + sku + ".jpg")
        .attributes(createProductAttributes())
        .build();
  }

  /**
   * Creates a CartItem with specific SKU and subSku.
   */
  public static CartItem createCartItem(String sku, String subSku, int quantity, long price) {
    return CartItem.builder()
        .sku(sku)
        .subSku(subSku)
        .title(faker.commerce().productName())
        .priceSnapshot(price)
        .quantity(quantity)
        .availableStockSnapshot(100)
        .imageUrl("https://example.com/images/" + sku + ".jpg")
        .attributes(createProductAttributes())
        .build();
  }

  /**
   * Creates random product attributes.
   */
  public static Map<String, String> createProductAttributes() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("size", faker.options().option("S", "M", "L", "XL"));
    attributes.put("color", faker.color().name());
    return attributes;
  }

  // ============================================================
  // Checkout and CheckoutItem
  // ============================================================

  /**
   * Creates a Checkout entity with WAITING status.
   */
  public static Checkout createCheckout(String userId) {
    String checkoutId = "chk-" + UUID.randomUUID().toString().substring(0, 8);
    return Checkout.builder()
        .id(UUID.randomUUID().toString())
        .checkoutId(checkoutId)
        .userId(userId)
        .sourceCartId(UUID.randomUUID().toString())
        .items(new ArrayList<>())
        .totalPrice(0L)
        .currency("IDR")
        .status("WAITING")
        .lockedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(900))
        .createdAt(Instant.now())
        .build();
  }

  /**
   * Creates a Checkout entity with items.
   */
  public static Checkout createCheckoutWithItems(String userId, int itemCount) {
    List<CheckoutItem> items = new ArrayList<>();
    long totalPrice = 0L;
    for (int i = 0; i < itemCount; i++) {
      CheckoutItem item = createCheckoutItem(true);
      items.add(item);
      totalPrice += item.getPriceSnapshot() * item.getQuantity();
    }
    
    String checkoutId = "chk-" + UUID.randomUUID().toString().substring(0, 8);
    return Checkout.builder()
        .id(UUID.randomUUID().toString())
        .checkoutId(checkoutId)
        .userId(userId)
        .sourceCartId(UUID.randomUUID().toString())
        .items(items)
        .totalPrice(totalPrice)
        .currency("IDR")
        .status("WAITING")
        .lockedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(900))
        .createdAt(Instant.now())
        .build();
  }

  /**
   * Creates a finalized Checkout (with orderId and paymentCode).
   */
  public static Checkout createFinalizedCheckout(String userId) {
    Checkout checkout = createCheckoutWithItems(userId, 2);
    checkout.setOrderId("ORD-20241208-ABCD");
    checkout.setPaymentCode("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    checkout.setShippingAddress(createAddressSnapshot());
    return checkout;
  }

  /**
   * Creates a PAID Checkout.
   */
  public static Checkout createPaidCheckout(String userId) {
    Checkout checkout = createFinalizedCheckout(userId);
    checkout.setStatus("PAID");
    checkout.setPaidAt(Instant.now());
    return checkout;
  }

  /**
   * Creates a CANCELLED Checkout.
   */
  public static Checkout createCancelledCheckout(String userId) {
    Checkout checkout = createCheckoutWithItems(userId, 2);
    checkout.setStatus("CANCELLED");
    checkout.setCancelledAt(Instant.now());
    return checkout;
  }

  /**
   * Creates an expired Checkout (WAITING with past expiresAt).
   */
  public static Checkout createExpiredCheckout(String userId) {
    Checkout checkout = createCheckoutWithItems(userId, 2);
    checkout.setExpiresAt(Instant.now().minusSeconds(100)); // Expired
    return checkout;
  }

  /**
   * Creates a CheckoutItem with random data.
   */
  public static CheckoutItem createCheckoutItem(boolean reserved) {
    String sku = "SKU-" + faker.number().digits(6);
    String subSku = sku + "-VAR-" + faker.number().digits(3);
    return CheckoutItem.builder()
        .sku(sku)
        .subSku(subSku)
        .title(faker.commerce().productName())
        .priceSnapshot((long) faker.number().numberBetween(10000, 1000000))
        .quantity(faker.number().numberBetween(1, 5))
        .availableStockSnapshot(faker.number().numberBetween(10, 100))
        .imageUrl("https://example.com/images/" + sku + ".jpg")
        .attributes(createProductAttributes())
        .reserved(reserved)
        .reservationError(reserved ? null : "Failed to lock inventory")
        .build();
  }

  /**
   * Creates a CheckoutItem with specific data.
   */
  public static CheckoutItem createCheckoutItem(String sku, String subSku, int quantity, long price, boolean reserved) {
    return CheckoutItem.builder()
        .sku(sku)
        .subSku(subSku)
        .title(faker.commerce().productName())
        .priceSnapshot(price)
        .quantity(quantity)
        .availableStockSnapshot(100)
        .imageUrl("https://example.com/images/" + sku + ".jpg")
        .attributes(createProductAttributes())
        .reserved(reserved)
        .reservationError(reserved ? null : "Failed to lock inventory")
        .build();
  }

  // ============================================================
  // Address Snapshot
  // ============================================================

  /**
   * Creates an AddressSnapshot with random data.
   */
  public static AddressSnapshot createAddressSnapshot() {
    return AddressSnapshot.builder()
        .recipientName(faker.name().fullName())
        .phone(faker.phoneNumber().cellPhone())
        .country("Indonesia")
        .province(faker.address().state())
        .city(faker.address().city())
        .district(faker.address().cityName())
        .subDistrict(faker.address().streetName())
        .postalCode(faker.address().zipCode())
        .street(faker.address().streetAddress())
        .notes(faker.address().secondaryAddress())
        .latitude((float) faker.number().randomDouble(7, -90, 90))
        .longitude((float) faker.number().randomDouble(7, -180, 180))
        .build();
  }

  // ============================================================
  // Utility Methods
  // ============================================================

  /**
   * Generates a random user ID.
   */
  public static String randomUserId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generates a random SKU.
   */
  public static String randomSku() {
    return "SKU-" + faker.number().digits(6);
  }

  /**
   * Generates a random subSku from a SKU.
   */
  public static String randomSubSku(String sku) {
    return sku + "-VAR-" + faker.number().digits(3);
  }

  /**
   * Generates a random checkout ID.
   */
  public static String randomCheckoutId() {
    return "chk-" + UUID.randomUUID().toString().substring(0, 8);
  }
}

