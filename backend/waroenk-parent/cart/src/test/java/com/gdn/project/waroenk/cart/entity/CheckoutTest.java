package com.gdn.project.waroenk.cart.entity;

import com.gdn.project.waroenk.cart.fixture.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Checkout Entity Unit Tests")
class CheckoutTest {

  private Checkout checkout;

  @BeforeEach
  void setUp() {
    checkout = TestDataFactory.createCheckoutWithItems("test-user", 3);
  }

  @Nested
  @DisplayName("calculateTotalPrice Tests")
  class CalculateTotalPriceTests {

    @Test
    @DisplayName("Should return 0 for empty items")
    void shouldReturnZeroForEmptyItems() {
      // Given
      checkout.setItems(new ArrayList<>());

      // When
      Long total = checkout.calculateTotalPrice();

      // Then
      assertThat(total).isZero();
    }

    @Test
    @DisplayName("Should calculate total only for reserved items")
    void shouldCalculateTotalOnlyForReservedItems() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, true));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, false)); // Not reserved
      checkout.setItems(items);

      // When
      Long total = checkout.calculateTotalPrice();

      // Then
      assertThat(total).isEqualTo(100000L); // Only 2 * 50000
    }

    @Test
    @DisplayName("Should handle null items list")
    void shouldHandleNullItemsList() {
      // Given
      checkout.setItems(null);

      // When
      Long total = checkout.calculateTotalPrice();

      // Then
      assertThat(total).isZero();
    }
  }

  @Nested
  @DisplayName("isExpired Tests")
  class IsExpiredTests {

    @Test
    @DisplayName("Should return true for expired WAITING checkout")
    void shouldReturnTrueForExpiredWaitingCheckout() {
      // Given
      checkout.setStatus("WAITING");
      checkout.setExpiresAt(Instant.now().minusSeconds(100));

      // When
      boolean expired = checkout.isExpired();

      // Then
      assertThat(expired).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-expired WAITING checkout")
    void shouldReturnFalseForNonExpiredWaitingCheckout() {
      // Given
      checkout.setStatus("WAITING");
      checkout.setExpiresAt(Instant.now().plusSeconds(100));

      // When
      boolean expired = checkout.isExpired();

      // Then
      assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("Should return false for PAID checkout even if past expiry")
    void shouldReturnFalseForPaidCheckout() {
      // Given
      checkout.setStatus("PAID");
      checkout.setExpiresAt(Instant.now().minusSeconds(100));

      // When
      boolean expired = checkout.isExpired();

      // Then
      assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("Should return false for CANCELLED checkout even if past expiry")
    void shouldReturnFalseForCancelledCheckout() {
      // Given
      checkout.setStatus("CANCELLED");
      checkout.setExpiresAt(Instant.now().minusSeconds(100));

      // When
      boolean expired = checkout.isExpired();

      // Then
      assertThat(expired).isFalse();
    }

    @Test
    @DisplayName("Should return false when expiresAt is null")
    void shouldReturnFalseWhenExpiresAtIsNull() {
      // Given
      checkout.setStatus("WAITING");
      checkout.setExpiresAt(null);

      // When
      boolean expired = checkout.isExpired();

      // Then
      assertThat(expired).isFalse();
    }
  }

  @Nested
  @DisplayName("getEffectiveStatus Tests")
  class GetEffectiveStatusTests {

    @Test
    @DisplayName("Should return EXPIRED for expired WAITING checkout")
    void shouldReturnExpiredForExpiredWaitingCheckout() {
      // Given
      checkout.setStatus("WAITING");
      checkout.setExpiresAt(Instant.now().minusSeconds(100));

      // When
      String effectiveStatus = checkout.getEffectiveStatus();

      // Then
      assertThat(effectiveStatus).isEqualTo("EXPIRED");
    }

    @Test
    @DisplayName("Should return WAITING for active checkout")
    void shouldReturnWaitingForActiveCheckout() {
      // Given
      checkout.setStatus("WAITING");
      checkout.setExpiresAt(Instant.now().plusSeconds(100));

      // When
      String effectiveStatus = checkout.getEffectiveStatus();

      // Then
      assertThat(effectiveStatus).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("Should return PAID for paid checkout")
    void shouldReturnPaidForPaidCheckout() {
      // Given
      checkout.setStatus("PAID");

      // When
      String effectiveStatus = checkout.getEffectiveStatus();

      // Then
      assertThat(effectiveStatus).isEqualTo("PAID");
    }
  }

  @Nested
  @DisplayName("isFullyReserved Tests")
  class IsFullyReservedTests {

    @Test
    @DisplayName("Should return true when all items are reserved")
    void shouldReturnTrueWhenAllReserved() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, true));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, true));
      checkout.setItems(items);

      // When
      boolean fullyReserved = checkout.isFullyReserved();

      // Then
      assertThat(fullyReserved).isTrue();
    }

    @Test
    @DisplayName("Should return false when some items are not reserved")
    void shouldReturnFalseWhenSomeNotReserved() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, true));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, false));
      checkout.setItems(items);

      // When
      boolean fullyReserved = checkout.isFullyReserved();

      // Then
      assertThat(fullyReserved).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty items")
    void shouldReturnFalseForEmptyItems() {
      // Given
      checkout.setItems(new ArrayList<>());

      // When
      boolean fullyReserved = checkout.isFullyReserved();

      // Then
      assertThat(fullyReserved).isFalse();
    }
  }

  @Nested
  @DisplayName("hasReservedItems Tests")
  class HasReservedItemsTests {

    @Test
    @DisplayName("Should return true when at least one item is reserved")
    void shouldReturnTrueWhenAtLeastOneReserved() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, true));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, false));
      checkout.setItems(items);

      // When
      boolean hasReserved = checkout.hasReservedItems();

      // Then
      assertThat(hasReserved).isTrue();
    }

    @Test
    @DisplayName("Should return false when no items are reserved")
    void shouldReturnFalseWhenNoItemsReserved() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, false));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, false));
      checkout.setItems(items);

      // When
      boolean hasReserved = checkout.hasReservedItems();

      // Then
      assertThat(hasReserved).isFalse();
    }
  }

  @Nested
  @DisplayName("getReservedItemCount Tests")
  class GetReservedItemCountTests {

    @Test
    @DisplayName("Should count reserved items correctly")
    void shouldCountReservedItemsCorrectly() {
      // Given
      List<CheckoutItem> items = new ArrayList<>();
      items.add(TestDataFactory.createCheckoutItem("SKU-001", "SKU-001-VAR-001", 2, 50000L, true));
      items.add(TestDataFactory.createCheckoutItem("SKU-002", "SKU-002-VAR-001", 3, 30000L, false));
      items.add(TestDataFactory.createCheckoutItem("SKU-003", "SKU-003-VAR-001", 1, 20000L, true));
      checkout.setItems(items);

      // When
      long count = checkout.getReservedItemCount();

      // Then
      assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return 0 for empty items")
    void shouldReturnZeroForEmptyItems() {
      // Given
      checkout.setItems(new ArrayList<>());

      // When
      long count = checkout.getReservedItemCount();

      // Then
      assertThat(count).isZero();
    }
  }
}

