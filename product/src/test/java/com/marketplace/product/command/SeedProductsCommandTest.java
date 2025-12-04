package com.marketplace.product.command;

import com.marketplace.product.command.impl.SeedProductsCommandImpl;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedProductsCommandTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SeedProductsCommandImpl command;

    @Test
    void seedProducts_WhenEmpty_Success() {
        when(productRepository.count()).thenReturn(0L);

        command.execute(null);

        verify(productRepository, times(1)).saveAll(anyList());
    }

    @Test
    void seedProducts_WhenNotEmpty_Skips() {
        when(productRepository.count()).thenReturn(5L);

        command.execute(null);

        verify(productRepository, never()).saveAll(anyList());
    }
}
