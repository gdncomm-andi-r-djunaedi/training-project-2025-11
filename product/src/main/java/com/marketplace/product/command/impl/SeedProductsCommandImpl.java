package com.marketplace.product.command.impl;

import com.marketplace.product.command.SeedProductsCommand;
import com.marketplace.product.document.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedProductsCommandImpl implements SeedProductsCommand {

    private final ProductRepository productRepository;

    @Override
    public Void execute(Void request) {
        log.info("Seeding products into database");

        if (productRepository.count() > 0) {
            log.info("Products already exist, skipping seed");
            return null;
        }

        Product[] products = {
                Product.builder()
                        .name("Laptop")
                        .description("High-performance laptop")
                        .price(BigDecimal.valueOf(999.99))
                        .category("Electronics")
                        .stock(10)
                        .build(),
                Product.builder()
                        .name("Smartphone")
                        .description("Latest smartphone model")
                        .price(BigDecimal.valueOf(699.99))
                        .category("Electronics")
                        .stock(20)
                        .build(),
                Product.builder()
                        .name("Headphones")
                        .description("Noise-cancelling headphones")
                        .price(BigDecimal.valueOf(199.99))
                        .category("Electronics")
                        .stock(15)
                        .build(),
                Product.builder()
                        .name("T-Shirt")
                        .description("Cotton t-shirt")
                        .price(BigDecimal.valueOf(19.99))
                        .category("Clothing")
                        .stock(50)
                        .build(),
                Product.builder()
                        .name("Jeans")
                        .description("Denim jeans")
                        .price(BigDecimal.valueOf(49.99))
                        .category("Clothing")
                        .stock(30)
                        .build()
        };

        productRepository.saveAll(Arrays.asList(products));
        log.info("Seeded {} products successfully", products.length);
        
        return null;
    }
}

