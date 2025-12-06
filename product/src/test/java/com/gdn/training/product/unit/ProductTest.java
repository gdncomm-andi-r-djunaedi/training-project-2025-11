package com.gdn.training.product.unit;

import com.gdn.training.product.domain.model.Product;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Product domain model.
 * Tests product creation and field access.
 */
public class ProductTest {

    @Test
    void shouldCreateProduct_WithValidData() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // Act
        Product product = new Product(
                productId,
                sellerId,
                "Laptop",
                "High-end gaming laptop",
                "electronics",
                25000000,
                5,
                Product.Status.ACTIVE,
                createdAt);

        // Assert
        assertEquals(productId, product.getProductId());
        assertEquals(sellerId, product.getSellerId());
        assertEquals("Laptop", product.getName());
        assertEquals("High-end gaming laptop", product.getDescription());
        assertEquals("electronics", product.getCategory());
        assertEquals(25000000, product.getPrice());
        assertEquals(5, product.getStock());
        assertEquals(Product.Status.ACTIVE, product.getStatus());
        assertEquals(createdAt, product.getCreatedAt());
    }

    @Test
    void shouldCreateProduct_WithMinimumStock() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        // Act
        Product product = new Product(
                productId, sellerId, "Limited Item", "Rare", "collectibles",
                1000000, 0, Product.Status.INACTIVE, Instant.now());

        // Assert
        assertEquals(0, product.getStock());
        assertEquals(Product.Status.INACTIVE, product.getStatus());
    }

    @Test
    void shouldSupportAllStatusValues() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant now = Instant.now();

        // Act & Assert - ACTIVE
        Product activeProduct = new Product(
                productId, sellerId, "Active Product", "desc", "cat",
                100, 10, Product.Status.ACTIVE, now);
        assertEquals(Product.Status.ACTIVE, activeProduct.getStatus());

        // Act & Assert - INACTIVE
        Product inactiveProduct = new Product(
                productId, sellerId, "Inactive Product", "desc", "cat",
                100, 10, Product.Status.INACTIVE, now);
        assertEquals(Product.Status.INACTIVE, inactiveProduct.getStatus());

        // Act & Assert - DELETED
        Product deletedProduct = new Product(
                productId, sellerId, "Deleted Product", "desc", "cat",
                100, 10, Product.Status.DELETED, now);
        assertEquals(Product.Status.DELETED, deletedProduct.getStatus());

        // Act & Assert - NOT_FOUND
        Product notFoundProduct = new Product(
                productId, sellerId, "Not Found Product", "desc", "cat",
                100, 10, Product.Status.NOT_FOUND, now);
        assertEquals(Product.Status.NOT_FOUND, notFoundProduct.getStatus());
    }

    @Test
    void shouldHandleDecimalPrices() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        // Act
        Product product = new Product(
                productId, sellerId, "Cheap Item", "Budget", "misc",
                99.99, 100, Product.Status.ACTIVE, Instant.now());

        // Assert
        assertEquals(99.99, product.getPrice(), 0.001);
    }

    @Test
    void shouldCreateProduct_WithEmptyDescription() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        // Act
        Product product = new Product(
                productId, sellerId, "No Desc Product", "", "cat",
                500, 20, Product.Status.ACTIVE, Instant.now());

        // Assert
        assertEquals("", product.getDescription());
        assertEquals("No Desc Product", product.getName());
    }

    @Test
    void statusEnum_ShouldHaveCorrectValues() {
        // Assert
        Product.Status[] statuses = Product.Status.values();
        assertEquals(4, statuses.length);
        assertEquals("ACTIVE", Product.Status.ACTIVE.name());
        assertEquals("INACTIVE", Product.Status.INACTIVE.name());
        assertEquals("DELETED", Product.Status.DELETED.name());
        assertEquals("NOT_FOUND", Product.Status.NOT_FOUND.name());
    }
}
