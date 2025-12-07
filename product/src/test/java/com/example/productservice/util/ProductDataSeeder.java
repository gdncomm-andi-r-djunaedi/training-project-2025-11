package com.example.productservice.util;

import com.example.productservice.entity.Product;
import com.example.productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProductDataSeeder {

    private static final String[] CATEGORIES = {
            "Electronics", "Clothing", "Books", "Home & Garden", "Sports",
            "Toys", "Automotive", "Health & Beauty", "Food & Beverage", "Office Supplies"
    };

    private static final String[] ADJECTIVES = {
            "Premium", "Deluxe", "Professional", "Compact", "Portable",
            "Wireless", "Smart", "Eco-Friendly", "Durable", "Lightweight"
    };

    private static final String[] PRODUCTS = {
            "Laptop", "Phone", "Tablet", "Watch", "Camera",
            "Headphones", "Speaker", "Monitor", "Keyboard", "Mouse",
            "Shirt", "Pants", "Shoes", "Jacket", "Hat",
            "Book", "Notebook", "Pen", "Backpack", "Bottle"
    };

    public static void seed(ProductRepository repository) {
        long count = repository.count();
        if (count >= 50000) {
            System.out.println("Database already has " + count + " products. Skipping seeding.");
            return;
        }

        System.out.println("Seeding 50,000 products...");
        Random random = new Random();
        List<Product> batch = new ArrayList<>();
        int batchSize = 1000;
        int[] tiers = {
                9900, 14900, 19900, 24900, 29900,
                49900, 74900, 99900, 149900, 199900,
                249900, 299900, 499900, 749900, 999900
        };

        for (int i = 1; i <= 50000; i++) {
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
            String productName = PRODUCTS[random.nextInt(PRODUCTS.length)];

            Product product = new Product();
            product.setName(adjective + " " + productName + " #" + i);
            product.setDescription(category + " - " + adjective + " " + productName + " with advanced features");

            int price = tiers[random.nextInt(tiers.length)];
            product.setPrice(BigDecimal.valueOf(price));

            batch.add(product);

            if (i % batchSize == 0) {
                repository.saveAll(batch);
                batch.clear();
                System.out.println("Seeded " + i + " products...");
            }
        }

        if (!batch.isEmpty()) {
            repository.saveAll(batch);
        }

        System.out.println("Seeding completed! Total products: " + repository.count());
    }
}
