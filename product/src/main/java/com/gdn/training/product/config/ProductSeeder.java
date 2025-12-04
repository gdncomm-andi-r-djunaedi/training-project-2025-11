package com.gdn.training.product.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gdn.training.product.model.entity.Product;
import com.gdn.training.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {

        if (productRepository.count() > 0) {
            System.out.println("Product table not empty. Seeder skipped.");
            return;
        }

        int totalProducts = 50000;
        int batchSize = 1000;
        int min = 1;
        int max = 1000;
        List<Product> batch = new ArrayList<>(batchSize);
        List<String> names = List.of(
                "Coffee", "Tea", "Green Tea", "Latte", "Espresso",
                "iPhone 14", "iPhone 15 Pro", "Samsung Galaxy S23",
                "Xiaomi Redmi Note", "MacBook Air", "MacBook Pro",
                "Keyboard Mechanical", "Gaming Mouse", "Water Bottle",
                "Bluetooth Speaker", "Smartwatch", "AirPods Pro");

        for (long i = 1; i <= totalProducts; i++) {
            String name = names.get(random.nextInt(names.size()));
            String randomizedName = name + " " + (1000 + random.nextInt(9000));

            Product p = new Product();
            p.setName(randomizedName);
            p.setDescription("High-quality " + name + " #" + i);
            p.setPrice(min + (max - min) * random.nextDouble());
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
