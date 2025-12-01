package com.gdn.training.product.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gdn.training.product.model.entity.Product;
import com.gdn.training.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        int totalProducts = 50000;
        int batchSize = 1000;
        List<Product> batch = new ArrayList<>(batchSize);

        for (long i = 1; i <= totalProducts; i++) {
            Product p = new Product();
            p.setName("Product " + i);
            p.setDescription("Description for product " + i);
            p.setPrice(10.0 * i);
            batch.add(p);

            if (batch.size() == batchSize) {
                productRepository.saveAll(batch);
                batch.clear();
                System.out.println("Inserted " + i + " products...");
            }
        }

        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
        }

        System.out.println("Seeding complete: " + totalProducts + " products inserted.");
    }

}
