package com.gdn.training.product.unit;

import com.gdn.training.product.domain.specification.ProductSpecification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductSpecification domain value object.
 * Tests that specifications are created correctly and getters work.
 */
public class ProductSpecificationTest {

    @Test
    void shouldCreateSpecification_WithAllParameters() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                "laptop", "electronics", 1000.0, 50000.0);

        // Assert
        assertEquals("laptop", spec.keyword());
        assertEquals("electronics", spec.category());
        assertEquals(1000.0, spec.minPrice());
        assertEquals(50000.0, spec.maxPrice());
    }

    @Test
    void shouldCreateSpecification_WithNullKeyword() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                null, "books", 0.0, 1000.0);

        // Assert
        assertNull(spec.keyword());
        assertEquals("books", spec.category());
        assertEquals(0.0, spec.minPrice());
        assertEquals(1000.0, spec.maxPrice());
    }

    @Test
    void shouldCreateSpecification_WithNullCategory() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                "gaming", null, 500.0, 2000.0);

        // Assert
        assertEquals("gaming", spec.keyword());
        assertNull(spec.category());
        assertEquals(500.0, spec.minPrice());
        assertEquals(2000.0, spec.maxPrice());
    }

    @Test
    void shouldCreateSpecification_WithZeroPrices() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                "cheap", "clearance", 0.0, 0.0);

        // Assert
        assertEquals("cheap", spec.keyword());
        assertEquals("clearance", spec.category());
        assertEquals(0.0, spec.minPrice());
        assertEquals(0.0, spec.maxPrice());
    }

    @Test
    void shouldCreateSpecification_WithAllNullFilters() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                null, null, 0.0, Double.MAX_VALUE);

        // Assert
        assertNull(spec.keyword());
        assertNull(spec.category());
        assertEquals(0.0, spec.minPrice());
        assertEquals(Double.MAX_VALUE, spec.maxPrice());
    }

    @Test
    void shouldHandleEmptyStrings() {
        // Act
        ProductSpecification spec = new ProductSpecification(
                "", "", 100.0, 500.0);

        // Assert
        assertEquals("", spec.keyword());
        assertEquals("", spec.category());
        assertEquals(100.0, spec.minPrice());
        assertEquals(500.0, spec.maxPrice());
    }
}
