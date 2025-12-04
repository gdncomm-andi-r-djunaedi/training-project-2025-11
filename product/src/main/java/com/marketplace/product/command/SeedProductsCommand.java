package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedProductsCommand implements Command<Void, Void> {

    private final ProductService productService;

    @Override
    public Void execute(Void request) {
        productService.seedProducts();
        return null;
    }
}
