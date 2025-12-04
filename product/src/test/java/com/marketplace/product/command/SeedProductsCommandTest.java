package com.marketplace.product.command;

import com.marketplace.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SeedProductsCommandTest {

    @Mock
    private ProductService productService;

    private SeedProductsCommand command;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new SeedProductsCommand(productService);
    }

    @Test
    void testSeedProductsCommand() {
        command.execute(null);

        verify(productService, times(1)).seedProducts();
    }
}
