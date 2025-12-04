package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SeedProductsCommand implements Command<Void> {

    private final ProductService productService;

    @Override
    public Void execute() {
        productService.seedProducts();
        return null;
    }
}
