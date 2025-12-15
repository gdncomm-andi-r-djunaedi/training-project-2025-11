package com.example.ecommerce.config;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final ProductRepository productRepository;

        @Override
        public void run(String... args) throws Exception {
                if (productRepository.count() == 0) {
                        productRepository.save(Product.builder()
                                        .name("iPhone 15")
                                        .description("Apple iPhone 15 with 128GB storage")
                                        .price(new BigDecimal("999.99"))
                                        .sku("IPHONE15-128")
                                        .build());

                        productRepository.save(Product.builder()
                                        .name("Samsung Galaxy S24")
                                        .description("Samsung Galaxy S24 Ultra")
                                        .price(new BigDecimal("1199.99"))
                                        .sku("S24-ULTRA")
                                        .build());

                        productRepository.save(Product.builder()
                                        .name("MacBook Pro")
                                        .description("M3 Pro MacBook Pro 14 inch")
                                        .price(new BigDecimal("1999.00"))
                                        .sku("MBP-M3-14")
                                        .build());

                        productRepository.save(Product.builder()
                                        .name("Sony WH-1000XM5")
                                        .description("Noise cancelling headphones")
                                        .price(new BigDecimal("348.00"))
                                        .sku("SONY-XM5")
                                        .build());
                }
        }
}
