package com.ecommerce.product.config;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Faker faker = new Faker();
            List<Product> products = new ArrayList<>();

            // Batch insert in chunks of 1000 to avoid memory issues
            int batchSize = 1000;
            int totalProducts = 50000;

            for (int i = 0; i < totalProducts; i++) {
                Product product = new Product();
                product.setSku(UUID.randomUUID().toString());
                product.setName(faker.commerce().productName());
                product.setDescription(faker.lorem().sentence());
                product.setPrice(new BigDecimal(faker.commerce().price().replace(",", ".")));
                product.setStock(faker.number().numberBetween(1, 100));

                products.add(product);

                if (products.size() >= batchSize) {
                    productRepository.saveAll(products);
                    products.clear();
                    System.out.println("Seeded " + (i + 1) + " products...");
                }
            }

            if (!products.isEmpty()) {
                productRepository.saveAll(products);
            }

            System.out.println("Finished seeding " + totalProducts + " products");
        }
    }
}
