// java
package com.dev.onlineMarketplace.ProductService.configs;

import com.dev.onlineMarketplace.ProductService.model.Product;
import com.dev.onlineMarketplace.ProductService.repository.ProductRepository;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;
    private final Faker faker;

    private static final int TOTAL_PRODUCTS = 50_000;
    private static final int BATCH_SIZE = 1_000;
    private static final String[] CATEGORIES = {
            "Electronics", "Books", "Home", "Garden", "Clothing", "Toys", "Sports", "Beauty", "Automotive"
    };

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.faker = new Faker(new Locale("en"));
    }

    private String generateSku(String category) {
        String prefix = category.substring(0, Math.min(3, category.length())).toUpperCase();
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + "-" + unique;
    }

    @Override
    public void run(String... args) throws Exception {
        long existing = productRepository.count();
        if (existing >= TOTAL_PRODUCTS) {
            log.info("Skipping data seeding - repository already contains {} products.", existing);
            return;
        }

        log.info("Starting data seeding: generating {} products...", TOTAL_PRODUCTS);

        List<Product> batch = new ArrayList<>(BATCH_SIZE);
        int generated = 0;

        while (generated < TOTAL_PRODUCTS) {
            String baseName = faker.commerce().productName();
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
            String name = baseName + " " + uniqueSuffix;

            String description = faker.lorem().sentence(8);
            double price = faker.number().randomDouble(2, 5, 2000);
            String category = CATEGORIES[faker.number().numberBetween(0, CATEGORIES.length)];
            String imageUrl = "https://picsum.photos/seed/" + uniqueSuffix + "/600/600";
            String sku = generateSku(category);

            Product p = new Product();
            p.setName(name);
            p.setDescription(description);
            p.setPrice(price);
            p.setCategory(category);
            p.setImageUrl(imageUrl);
            p.setSku(sku);

            batch.add(p);
            generated++;

            if (batch.size() >= BATCH_SIZE) {
                productRepository.saveAll(batch);
                batch.clear();
                log.info("Inserted {} products so far...", generated);
            }
        }

        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
            log.info("Inserted final batch. Total inserted: {}", generated);
        }

        log.info("Data seeding completed.");
    }
}
