package com.marketplace.seeder.command.impl;

import com.marketplace.product.document.Product;
import com.marketplace.product.document.ProductSearchDoc;
import com.marketplace.product.repository.ProductRepository;
import com.marketplace.product.repository.ProductSearchRepository;
import com.marketplace.seeder.command.SeedProductsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedProductsCommandImpl implements SeedProductsCommand {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final Faker faker;

    private static final int BATCH_SIZE = 1000;

    private static final String[] CATEGORIES = {
            "Electronics",
            "Clothing",
            "Home & Garden",
            "Sports & Outdoors",
            "Books",
            "Toys & Games",
            "Beauty & Personal Care",
            "Automotive",
            "Food & Beverages",
            "Health & Wellness"
    };

    private static final String[][] PRODUCT_TEMPLATES = {
            // Electronics
            {"Wireless %s Headphones", "Bluetooth %s Speaker", "Smart %s Watch", "%s Laptop", "%s Smartphone",
                    "%s Tablet", "USB %s Charger", "%s Monitor", "%s Keyboard", "%s Mouse"},
            // Clothing
            {"Cotton %s T-Shirt", "%s Denim Jeans", "%s Hoodie", "%s Dress", "%s Jacket",
                    "%s Sneakers", "%s Boots", "%s Cap", "%s Scarf", "%s Sweater"},
            // Home & Garden
            {"%s Lamp", "%s Pillow Set", "%s Curtains", "%s Rug", "%s Plant Pot",
                    "%s Garden Tools", "%s Bedding Set", "%s Wall Art", "%s Storage Box", "%s Chair"},
            // Sports & Outdoors
            {"%s Yoga Mat", "%s Dumbbells", "%s Running Shoes", "%s Backpack", "%s Tent",
                    "%s Bicycle", "%s Football", "%s Tennis Racket", "%s Swimming Goggles", "%s Fitness Tracker"},
            // Books
            {"The Art of %s", "%s: A Complete Guide", "Learning %s", "%s for Beginners", "Advanced %s",
                    "The %s Cookbook", "%s Stories", "History of %s", "%s Encyclopedia", "The %s Journey"},
            // Toys & Games
            {"%s Building Blocks", "%s Board Game", "%s Puzzle", "%s Action Figure", "%s Doll",
                    "%s Remote Car", "%s Card Game", "%s Plush Toy", "%s Science Kit", "%s Art Set"},
            // Beauty & Personal Care
            {"%s Face Cream", "%s Shampoo", "%s Perfume", "%s Lipstick", "%s Foundation",
                    "%s Hair Oil", "%s Body Lotion", "%s Nail Polish", "%s Eye Shadow", "%s Sunscreen"},
            // Automotive
            {"%s Car Cover", "%s Floor Mats", "%s Phone Mount", "%s Seat Cushion", "%s Air Freshener",
                    "%s Dash Cam", "%s Tool Kit", "%s Jump Starter", "%s Tire Inflator", "%s Car Charger"},
            // Food & Beverages
            {"Organic %s Tea", "%s Coffee Beans", "Gourmet %s Chocolate", "%s Honey", "%s Olive Oil",
                    "%s Protein Powder", "%s Energy Bars", "%s Dried Fruits", "%s Spice Mix", "%s Snack Pack"},
            // Health & Wellness
            {"%s Vitamins", "%s Supplements", "%s Essential Oil", "%s Massage Tool", "%s First Aid Kit",
                    "%s Thermometer", "%s Blood Pressure Monitor", "%s Heating Pad", "%s Ice Pack", "%s Pill Organizer"}
    };

    @Override
    public Integer execute(Integer targetCount) {
        long existingCount = productRepository.count();

        if (existingCount >= targetCount) {
            log.info("Products already exist: {} (target: {}). Skipping seed.", existingCount, targetCount);
            return 0;
        }

        int productsToCreate = targetCount - (int) existingCount;
        log.info("Starting to seed {} products...", productsToCreate);

        List<Product> batch = new ArrayList<>(BATCH_SIZE);
        int createdCount = 0;
        int batchNumber = 1;

        for (int i = 0; i < productsToCreate; i++) {
            Product product = generateProduct(i);
            batch.add(product);

            if (batch.size() >= BATCH_SIZE) {
                List<Product> savedProducts = productRepository.saveAll(batch);
                saveToElasticSearch(savedProducts);
                createdCount += batch.size();
                log.info("Batch {} completed. Progress: {}/{}", batchNumber++, createdCount, productsToCreate);
                batch.clear();
            }
        }

        // Save remaining products
        if (!batch.isEmpty()) {
            List<Product> savedProducts = productRepository.saveAll(batch);
            saveToElasticSearch(savedProducts);
            createdCount += batch.size();
            log.info("Final batch completed. Total created: {}", createdCount);
        }

        log.info("Successfully seeded {} products", createdCount);
        return createdCount;
    }

    private Product generateProduct(int index) {
        int categoryIndex = index % CATEGORIES.length;
        String category = CATEGORIES[categoryIndex];
        String[] templates = PRODUCT_TEMPLATES[categoryIndex];

        String template = templates[faker.number().numberBetween(0, templates.length)];
        String adjective = faker.commerce().material();
        String productName = String.format(template, adjective);

        // Add unique suffix to ensure uniqueness
        productName = productName + " " + faker.color().name() + " #" + (index + 1);

        BigDecimal price = generatePrice(categoryIndex);
        String description = generateDescription(productName, category);

        return Product.builder()
                .name(productName)
                .description(description)
                .price(price)
                .category(category)
                .stock(faker.number().numberBetween(10, 1000))
                .build();
    }

    private BigDecimal generatePrice(int categoryIndex) {
        // Different price ranges for different categories
        double minPrice, maxPrice;
        switch (categoryIndex) {
            case 0: // Electronics
                minPrice = 29.99;
                maxPrice = 1999.99;
                break;
            case 1: // Clothing
                minPrice = 9.99;
                maxPrice = 299.99;
                break;
            case 2: // Home & Garden
                minPrice = 14.99;
                maxPrice = 499.99;
                break;
            case 3: // Sports & Outdoors
                minPrice = 19.99;
                maxPrice = 599.99;
                break;
            case 4: // Books
                minPrice = 7.99;
                maxPrice = 79.99;
                break;
            case 5: // Toys & Games
                minPrice = 9.99;
                maxPrice = 149.99;
                break;
            case 6: // Beauty & Personal Care
                minPrice = 4.99;
                maxPrice = 199.99;
                break;
            case 7: // Automotive
                minPrice = 9.99;
                maxPrice = 299.99;
                break;
            case 8: // Food & Beverages
                minPrice = 4.99;
                maxPrice = 99.99;
                break;
            case 9: // Health & Wellness
                minPrice = 9.99;
                maxPrice = 149.99;
                break;
            default:
                minPrice = 9.99;
                maxPrice = 199.99;
        }

        double price = faker.number().randomDouble(2, (int) minPrice, (int) maxPrice);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateDescription(String productName, String category) {
        String quality = faker.options().option("Premium", "High-quality", "Professional", "Deluxe", "Essential", "Classic", "Modern", "Innovative");
        String feature1 = faker.options().option("durable", "lightweight", "ergonomic", "stylish", "eco-friendly", "versatile", "compact", "reliable");
        String feature2 = faker.options().option("easy to use", "long-lasting", "affordable", "top-rated", "best-selling", "customer favorite", "highly recommended", "value for money");

        return String.format("%s %s in %s category. Features: %s and %s. %s",
                quality, productName, category, feature1, feature2, faker.lorem().sentence(10));
    }

    private void saveToElasticSearch(List<Product> products) {
        List<ProductSearchDoc> searchDocs = products.stream()
                .map(p -> ProductSearchDoc.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .category(p.getCategory())
                        .stock(p.getStock())
                        .build())
                .toList();
        productSearchRepository.saveAll(searchDocs);
    }
}

