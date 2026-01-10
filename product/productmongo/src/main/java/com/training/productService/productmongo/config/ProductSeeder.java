package com.training.productService.productmongo.config;

import com.github.javafaker.Faker;
import com.training.productService.productmongo.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProductSeeder implements CommandLineRunner {
    @Autowired
    private MongoTemplate mongoTemplate;
    private final Faker faker = new Faker();

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;
    private static final long TARGET_COUNT = 50000;

    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Product seeding disabled (app.seed.enabled=false). Skipping.");
            return;
        }
        long existingProductCount = mongoTemplate.getCollection("products").countDocuments();
        if (existingProductCount >= TARGET_COUNT) {
            log.info("Product collection already has {} products (>= {}). No seeding needed.", existingProductCount,
                    TARGET_COUNT);
            return;
        }

        long remainingProductsToCreate = TARGET_COUNT - existingProductCount;
        log.info("Seeding {} products to reach target of {}...", remainingProductsToCreate, TARGET_COUNT);

        List<Product> products = new ArrayList<>((int) remainingProductsToCreate);
        long nextProgress = remainingProductsToCreate / 10;
        long nextCheckpoint = nextProgress;
        for (int i = 0; i < remainingProductsToCreate; i++) {
            products.add(generateRandomProduct());
            if (i >= nextCheckpoint) {
                int percent = (int) ((i * 100) / remainingProductsToCreate);
                log.info("Seeding progress: {}% ({} / {})", percent, i, remainingProductsToCreate);
                nextCheckpoint += nextProgress;
            }
        }
        mongoTemplate.insert(products, Product.class);
        log.info("Successfully seeded {} products. Total products now: {}", remainingProductsToCreate, mongoTemplate.getCollection("products").countDocuments());
    }

    private Product generateRandomProduct() {
        Product p = new Product();
        p.setId(null);
        p.setName(faker.commerce().productName());
        p.setDescription(faker.lorem().sentence(12));
        p.setPrice(Double.valueOf(faker.commerce().price()));
        p.setCategory(faker.commerce().department());
        List<String> tags = new ArrayList<>();
        int tagCount = faker.number().numberBetween(3, 6);
        for (int i = 0; i < tagCount; i++) {
            tags.add(faker.commerce().material());
        }
        p.setTags(tags);
        List<String> images = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            images.add("https://picsum.photos/seed/" + faker.random().hex() + "/600/600");
        }
        p.setImages(images);
        String sku = faker.bothify("??###??#").toUpperCase();
        p.setSku(sku);
        return p;
    }
}
