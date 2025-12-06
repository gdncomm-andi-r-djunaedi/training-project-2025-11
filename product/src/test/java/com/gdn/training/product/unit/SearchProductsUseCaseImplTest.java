package com.gdn.training.product.unit;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;
import com.gdn.training.product.domain.specification.ProductSpecification;
import com.gdn.training.product.infrastructure.usecase.SearchProductsUseCaseImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchProductsUseCaseImpl.
 * Uses mocked ProductRepository to test search logic in isolation.
 */
public class SearchProductsUseCaseImplTest {

    @Test
    void shouldReturnMappedProductResponses_WhenRepositoryReturnsProducts() {
        // Arrange
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        SearchProductsUseCaseImpl useCase = new SearchProductsUseCaseImpl(mockRepo);

        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant now = Instant.now();

        Product product1 = new Product(
                productId1, sellerId, "Laptop", "High-end laptop", "electronics",
                15000000, 10, Product.Status.ACTIVE, now);
        Product product2 = new Product(
                productId2, sellerId, "Mouse", "Wireless mouse", "electronics",
                200000, 50, Product.Status.ACTIVE, now);

        List<Product> products = List.of(product1, product2);
        ProductSpecification spec = new ProductSpecification("laptop", "electronics", 0, Double.MAX_VALUE);

        when(mockRepo.search(any(ProductSpecification.class), anyInt(), anyInt(), isNull()))
                .thenReturn(products);

        // Act
        List<ProductResponse> responses = useCase.execute(spec, 0, 10);

        // Assert
        assertEquals(2, responses.size());
        assertEquals(productId1.toString(), responses.get(0).productId());
        assertEquals("Laptop", responses.get(0).name());
        assertEquals(15000000, responses.get(0).price());
        assertEquals("ACTIVE", responses.get(0).status());

        assertEquals(productId2.toString(), responses.get(1).productId());
        assertEquals("Mouse", responses.get(1).name());

        verify(mockRepo, times(1)).search(spec, 0, 10, null);
    }

    @Test
    void shouldReturnEmptyList_WhenRepositoryReturnsNoProducts() {
        // Arrange
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        SearchProductsUseCaseImpl useCase = new SearchProductsUseCaseImpl(mockRepo);

        ProductSpecification spec = new ProductSpecification("nonexistent", null, 0, 0);

        when(mockRepo.search(any(ProductSpecification.class), anyInt(), anyInt(), isNull()))
                .thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> responses = useCase.execute(spec, 0, 10);

        // Assert
        assertTrue(responses.isEmpty());
        verify(mockRepo, times(1)).search(spec, 0, 10, null);
    }

    @Test
    void shouldPassCorrectPaginationParameters_ToRepository() {
        // Arrange
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        SearchProductsUseCaseImpl useCase = new SearchProductsUseCaseImpl(mockRepo);

        ProductSpecification spec = new ProductSpecification(null, "electronics", 1000, 50000);
        when(mockRepo.search(any(), anyInt(), anyInt(), isNull()))
                .thenReturn(Collections.emptyList());

        // Act
        useCase.execute(spec, 2, 20);

        // Assert
        verify(mockRepo, times(1)).search(spec, 2, 20, null);
    }

    @Test
    void shouldMapAllProductFields_Correctly() {
        // Arrange
        ProductRepository mockRepo = Mockito.mock(ProductRepository.class);
        SearchProductsUseCaseImpl useCase = new SearchProductsUseCaseImpl(mockRepo);

        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant createdAt = Instant.ofEpochMilli(1700000000000L);

        Product product = new Product(
                productId, sellerId, "Test Product", "Test Description", "test-category",
                999.99, 42, Product.Status.INACTIVE, createdAt);

        when(mockRepo.search(any(), anyInt(), anyInt(), isNull()))
                .thenReturn(List.of(product));

        ProductSpecification spec = new ProductSpecification(null, null, 0, 0);

        // Act
        List<ProductResponse> responses = useCase.execute(spec, 0, 10);

        // Assert
        ProductResponse response = responses.get(0);
        assertEquals(productId.toString(), response.productId());
        assertEquals(sellerId.toString(), response.sellerId());
        assertEquals("Test Product", response.name());
        assertEquals("Test Description", response.description());
        assertEquals("test-category", response.category());
        assertEquals(999.99, response.price());
        assertEquals(42, response.stock());
        assertEquals("INACTIVE", response.status());
        assertEquals(1700000000000L, response.createdAt());
    }
}
