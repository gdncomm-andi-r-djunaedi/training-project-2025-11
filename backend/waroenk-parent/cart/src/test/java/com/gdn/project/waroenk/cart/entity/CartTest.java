package com.gdn.project.waroenk.cart.entity;

import com.gdn.project.waroenk.cart.fixture.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cart Entity Unit Tests")
class CartTest {

  private Cart cart;

  @BeforeEach
  void setUp() {
    cart = TestDataFactory.createEmptyCart();
  }

  @Nested
  @DisplayName("getTotalAmount Tests")
  class GetTotalAmountTests {

    @Test
    @DisplayName("Should return 0 for empty cart")
    void shouldReturnZeroForEmptyCart() {
      assertThat(cart.getTotalAmount()).isZero();
    }

    @Test
    @DisplayName("Should calculate total correctly for single item")
    void shouldCalculateTotalForSingleItem() {
      // Given
      CartItem item = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L);
      cart.addOrUpdateItem(item);

      // When
      Long total = cart.getTotalAmount();

      // Then
      assertThat(total).isEqualTo(100000L); // 2 * 50000
    }

    @Test
    @DisplayName("Should calculate total correctly for multiple items")
    void shouldCalculateTotalForMultipleItems() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));

      // When
      Long total = cart.getTotalAmount();

      // Then
      assertThat(total).isEqualTo(190000L); // (2 * 50000) + (3 * 30000)
    }

    @Test
    @DisplayName("Should handle null items list")
    void shouldHandleNullItemsList() {
      // Given
      cart.setItems(null);

      // When
      Long total = cart.getTotalAmount();

      // Then
      assertThat(total).isZero();
    }
  }

  @Nested
  @DisplayName("getTotalItems Tests")
  class GetTotalItemsTests {

    @Test
    @DisplayName("Should return 0 for empty cart")
    void shouldReturnZeroForEmptyCart() {
      assertThat(cart.getTotalItems()).isZero();
    }

    @Test
    @DisplayName("Should count total quantity correctly")
    void shouldCountTotalQuantityCorrectly() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));

      // When
      Integer total = cart.getTotalItems();

      // Then
      assertThat(total).isEqualTo(5); // 2 + 3
    }
  }

  @Nested
  @DisplayName("addOrUpdateItem Tests")
  class AddOrUpdateItemTests {

    @Test
    @DisplayName("Should add new item to empty cart")
    void shouldAddNewItemToEmptyCart() {
      // Given
      CartItem item = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L);

      // When
      cart.addOrUpdateItem(item);

      // Then
      assertThat(cart.getItems()).hasSize(1);
      assertThat(cart.getItems().get(0).getSku()).isEqualTo("SKU-001");
    }

    @Test
    @DisplayName("Should update quantity for existing SKU")
    void shouldUpdateQuantityForExistingSku() {
      // Given
      CartItem item1 = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L);
      cart.addOrUpdateItem(item1);

      CartItem item2 = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 3, 50000L);

      // When
      cart.addOrUpdateItem(item2);

      // Then
      assertThat(cart.getItems()).hasSize(1);
      assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("Should add multiple different items")
    void shouldAddMultipleDifferentItems() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));

      // Then
      assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should update price snapshot for existing item")
    void shouldUpdatePriceSnapshotForExistingItem() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      CartItem updatedItem = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 1, 60000L);

      // When
      cart.addOrUpdateItem(updatedItem);

      // Then
      assertThat(cart.getItems().get(0).getPriceSnapshot()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("Should initialize items list if null")
    void shouldInitializeItemsListIfNull() {
      // Given
      cart.setItems(null);
      CartItem item = TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L);

      // When
      cart.addOrUpdateItem(item);

      // Then
      assertThat(cart.getItems()).isNotNull();
      assertThat(cart.getItems()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("removeItem Tests")
  class RemoveItemTests {

    @Test
    @DisplayName("Should remove existing item by SKU")
    void shouldRemoveExistingItemBySku() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));

      // When
      boolean removed = cart.removeItem("SKU-001");

      // Then
      assertThat(removed).isTrue();
      assertThat(cart.getItems()).hasSize(1);
      assertThat(cart.getItems().get(0).getSku()).isEqualTo("SKU-002");
    }

    @Test
    @DisplayName("Should return false for non-existent SKU")
    void shouldReturnFalseForNonExistentSku() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      // When
      boolean removed = cart.removeItem("NON-EXISTENT");

      // Then
      assertThat(removed).isFalse();
      assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should return false for null items list")
    void shouldReturnFalseForNullItemsList() {
      // Given
      cart.setItems(null);

      // When
      boolean removed = cart.removeItem("SKU-001");

      // Then
      assertThat(removed).isFalse();
    }
  }

  @Nested
  @DisplayName("updateItemQuantity Tests")
  class UpdateItemQuantityTests {

    @Test
    @DisplayName("Should update quantity for existing item")
    void shouldUpdateQuantityForExistingItem() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      // When
      boolean updated = cart.updateItemQuantity("SKU-001", 5);

      // Then
      assertThat(updated).isTrue();
      assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should remove item when quantity is 0")
    void shouldRemoveItemWhenQuantityIsZero() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      // When
      boolean updated = cart.updateItemQuantity("SKU-001", 0);

      // Then
      assertThat(updated).isTrue();
      assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should remove item when quantity is negative")
    void shouldRemoveItemWhenQuantityIsNegative() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      // When
      boolean updated = cart.updateItemQuantity("SKU-001", -1);

      // Then
      assertThat(updated).isTrue();
      assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should return false for non-existent SKU")
    void shouldReturnFalseForNonExistentSku() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));

      // When
      boolean updated = cart.updateItemQuantity("NON-EXISTENT", 5);

      // Then
      assertThat(updated).isFalse();
    }
  }

  @Nested
  @DisplayName("clearItems Tests")
  class ClearItemsTests {

    @Test
    @DisplayName("Should clear all items")
    void shouldClearAllItems() {
      // Given
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));

      // When
      cart.clearItems();

      // Then
      assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null items list")
    void shouldHandleNullItemsList() {
      // Given
      cart.setItems(null);

      // When - Should not throw
      cart.clearItems();

      // Then - No exception
    }
  }
}

