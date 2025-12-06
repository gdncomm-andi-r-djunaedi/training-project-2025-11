package com.gdn.training.product.unit;

import com.gdn.training.product.application.dto.ProductResponse;
import com.gdn.training.product.domain.exception.ProductNotFoundException;
import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;
import com.gdn.training.product.infrastructure.usecase.GetProductByIdUseCaseImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetProductByIdUseCaseImplTest {
    @Test
    void shouldReturnProductResponse_WhenProductExists() {
        // Arrange
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        GetProductByIdUseCaseImpl useCase = new GetProductByIdUseCaseImpl(repo);

        UUID productId = UUID.randomUUID();
        Product domain = new Product(
                productId,
                UUID.randomUUID(),
                "Laptop A",
                "Nice laptop",
                "electronics",
                12000000,
                5,
                Product.Status.ACTIVE,
                Instant.now());

        when(repo.findById(productId)).thenReturn(Optional.of(domain));

        // Act
        ProductResponse response = useCase.execute(productId.toString());

        // Assert
        assertEquals(domain.getProductId().toString(), response.productId());
        assertEquals(domain.getName(), response.name());
        verify(repo, times(1)).findById(productId);
    }

    @Test
    void shouldThrowProductNotFound_WhenRepositoryReturnsEmpty() {
        // Arrange
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        GetProductByIdUseCaseImpl useCase = new GetProductByIdUseCaseImpl(repo);

        UUID productId = UUID.randomUUID();
        when(repo.findById(productId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ProductNotFoundException.class, () -> useCase.execute(productId.toString()));
    }
}
