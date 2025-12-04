package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.common.command.CommandInvoker;
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

    private CommandInvoker invoker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invoker = new CommandInvoker();
    }

    @Test
    void testSeedProductsCommand() {
        SeedProductsCommand command = new SeedProductsCommand(productService);
        invoker.executeCommand(command);

        verify(productService, times(1)).seedProducts();
    }
}
