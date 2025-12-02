package com.gdn.training.product.config;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing product data...");

        // 1. Remove older data
        productRepository.deleteAll();
        log.info("Cleared existing product data.");

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            String sequence = String.format("%010d", i);
            String sku = "TES" + sequence;
            String name = "test-data" + sequence;

            Product product = Product.builder()
                    .productSku(sku)
                    .productName(name)
                    .quantity(999)
                    .price(new BigDecimal("100.00")) // Default price
                    .description("Sample product " + i)
                    .category("General")
                    .imageUrl("https://example.com/placeholder.png")
                    .createdTime(new java.util.Date())
                    .build();
            products.add(product);
        }

        productRepository.saveAll(products);
        log.info("Inserted {} sample products.", products.size());
    }
}
