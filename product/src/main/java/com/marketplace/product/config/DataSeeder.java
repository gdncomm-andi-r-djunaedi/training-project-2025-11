package com.marketplace.product.config;

import com.github.javafaker.Faker;
import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")  // Don't run during tests
public class DataSeeder implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    @Value("${data.seed.enabled:false}")
    private boolean seedEnabled;
    
    @Value("${data.seed.products-count:50000}")
    private int productsCount;
    
    private static final String[] CATEGORIES = {
        "Electronics", "Computers", "Smartphones", "Tablets", "Cameras",
        "Home & Kitchen", "Furniture", "Appliances", "Decor",
        "Clothing", "Men's Fashion", "Women's Fashion", "Kids' Fashion", "Accessories",
        "Books", "Fiction", "Non-Fiction", "Educational", "Comics",
        "Sports & Outdoors", "Exercise Equipment", "Camping", "Sports Gear",
        "Beauty & Personal Care", "Skincare", "Makeup", "Haircare",
        "Toys & Games", "Board Games", "Action Figures", "Puzzles",
        "Automotive", "Car Parts", "Accessories", "Tools"
    };
    
    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Product data seeding is disabled");
            return;
        }
        
        long existingCount = productRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} products, skipping seeding", existingCount);
            return;
        }
        
        log.info("Starting to seed {} products...", productsCount);
        long startTime = System.currentTimeMillis();
        
        Faker faker = new Faker();
        Random random = new Random();
        List<Product> products = new ArrayList<>();
        
        for (int i = 0; i < productsCount; i++) {
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            
            Product product = Product.builder()
                    .name(generateProductName(faker, category))
                    .description(faker.lorem().sentence(20))
                    .category(category)
                    .price(generatePrice(random))
                    .imageUrl(generateImageUrl(i))
                    .stock(9999)  // Unlimited stock
                    .build();
            
            products.add(product);
            
            // Save in batches of 5000 for better performance
            if (products.size() >= 5000) {
                productRepository.saveAll(products);
                products.clear();
                log.info("Seeded {} products...", i + 1);
            }
        }
        
        // Save remaining products
        if (!products.isEmpty()) {
            productRepository.saveAll(products);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Database seeding completed! Created {} products in {} seconds",
                productsCount, duration / 1000.0);
    }
    
    private String generateProductName(Faker faker, String category) {
        return switch (category.toLowerCase()) {
            case "electronics", "computers" -> faker.company().name() + " " + faker.commerce().productName();
            case "smartphones" -> faker.company().name() + " Smartphone " + faker.random().nextInt(1, 15);
            case "clothing", "men's fashion", "women's fashion", "kids' fashion" ->
                    faker.commerce().color() + " " + faker.commerce().material() + " " + faker.commerce().productName();
            case "books", "fiction", "non-fiction" -> faker.book().title();
            default -> faker.commerce().productName();
        };
    }
    
    private double generatePrice(Random random) {
        // Generate prices between $10 and $9999
        double price = 10 + (9989 * random.nextDouble());
        return Math.round(price * 100.0) / 100.0;  // Round to 2 decimal places
    }
    
    private String generateImageUrl(int index) {
        // Use placeholder images
        return "https://via.placeholder.com/400x400?text=Product+" + (index + 1);
    }
}
