package com.blibli.productmodule;

import com.blibli.productmodule.entity.ProductSearch;
import com.blibli.productmodule.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class ProductDataSeederApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductDataSeederApplication.class, args);
    }
}

@Slf4j
//@Component
class ProductDataSeeder implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    private final Random random = new Random();
    
    private static final String[] PRODUCT_TYPES = {
        "Laptop", "Smartphone", "Tablet", "Headphones", "Keyboard", "Mouse", "Monitor",
        "Camera", "Speaker", "Watch", "Charger", "Cable", "Case", "Stand", "Bag",
        "Phone", "Mobile Phone", "Gaming Laptop", "Wireless Headphones", "Mechanical Keyboard",
        "Gaming Mouse", "4K Monitor", "DSLR Camera", "Bluetooth Speaker", "Smart Watch"
    };
    
    private static final String[] BRANDS = {
        "TechPro", "SmartTech", "DigitalPlus", "EliteGear", "ProMax", "UltraTech",
        "MegaBrand", "SuperGadget", "PowerTech", "PrimeDevice", "ApexTech", "Nexus"
    };
    
    private static final String[] ADJECTIVES = {
        "Premium", "Pro", "Ultra", "Max", "Plus", "Elite", "Advanced", "Super",
        "Deluxe", "Professional", "High-End", "Standard", "Basic", "Compact",
        "Gaming", "Wireless", "Bluetooth", "Smart", "Portable", "Ergonomic"
    };
    
    private static final String[] SEARCH_TERMS = {
        "laptop", "phone", "headphone", "keyboard", "mouse", "monitor", "camera",
        "speaker", "watch", "gaming", "wireless", "bluetooth", "smart", "portable"
    };
    
    private static final String[] CATEGORIES = {
        "Electronics", "Computers", "Mobile", "Accessories", "Audio", "Photography",
        "Gaming", "Office", "Home", "Travel", "Sports", "Fashion"
    };
    
    @Override
    public void run(String... args) {
        log.info("Checking existing products in database...");
        
        int startIndex = findNextAvailableProductCode();
        log.info("Starting to create products from PRD-{}...", String.format("%05d", startIndex));
        
        int totalProducts = 50000;
        int count = 0;
        int batchSize = 1000;
        List<ProductSearch> batch = new ArrayList<>();
        
        for (int i = startIndex; i <= totalProducts; i++) {
            try {
                String productCode = String.format("PRD-%05d", i);
                ProductSearch existing = productRepository.findByProductCode(productCode);
                if (existing != null) {
                    log.debug("Product {} already exists, skipping...", productCode);
                    continue;
                }
                
                ProductSearch product = createRandomProduct(i, i <= startIndex + 1000);
                batch.add(product);
                
                if (batch.size() >= batchSize) {
                    try {
                        productRepository.saveAll(batch);
                        count += batch.size();
                        batch.clear();
                        log.info("Created {} products...", count);
                    } catch (Exception batchError) {
                        log.error("Batch insert error, trying individual inserts: {}", batchError.getMessage());
                        for (ProductSearch p : batch) {
                            try {
                                if (productRepository.findByProductCode(p.getProductCode()) == null) {
                                    productRepository.save(p);
                                    count++;
                                }
                            } catch (Exception individualError) {
                                log.error("Error creating product {}: {}", p.getProductCode(), individualError.getMessage());
                            }
                        }
                        batch.clear();
                    }
                }
            } catch (Exception e) {
                log.error("Error creating product {}: {}", i, e.getMessage());
            }
        }
        
        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
            count += batch.size();
        }
        
        log.info("Successfully created {} product records!", count);
        System.exit(0);
    }
    
    private int findNextAvailableProductCode() {
        try {
            List<ProductSearch> allProducts = productRepository.findAll();
            int maxCode = 0;
            
            for (ProductSearch product : allProducts) {
                String productCode = product.getProductCode();
                if (productCode != null && productCode.startsWith("PRD-")) {
                    try {
                        String numberPart = productCode.substring(4);
                        int codeNumber = Integer.parseInt(numberPart);
                        if (codeNumber > maxCode) {
                            maxCode = codeNumber;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
            
            int nextIndex = maxCode + 1;
            log.info("Found {} existing products. Highest code: PRD-{}. Starting from: PRD-{}", 
                       allProducts.size(), String.format("%05d", maxCode), String.format("%05d", nextIndex));
            return nextIndex;
        } catch (Exception e) {
            log.error("Error finding next product code, starting from 1: {}", e.getMessage());
            return 1;
        }
    }
    
    private ProductSearch createRandomProduct(int index, boolean includeSearchTerms) {
        String productType = PRODUCT_TYPES[random.nextInt(PRODUCT_TYPES.length)];
        String brand = BRANDS[random.nextInt(BRANDS.length)];
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        
        String productCode = String.format("PRD-%05d", index);
        String name;
        String description;
        
        if (includeSearchTerms && random.nextDouble() < 0.3) {
            String searchTerm = SEARCH_TERMS[random.nextInt(SEARCH_TERMS.length)];
            name = adjective + " " + searchTerm + " " + productType + " " + (1000 + random.nextInt(9000));
            description = "High-quality " + searchTerm + " " + productType.toLowerCase() + " from " + brand + 
                         ". Features advanced " + searchTerm + " technology and premium design. " +
                         "Perfect " + searchTerm + " solution for everyday use. Best " + searchTerm + " device available.";
        } else {
            name = adjective + " " + productType + " " + (1000 + random.nextInt(9000));
            description = "High-quality " + productType.toLowerCase() + " from " + brand + 
                         ". Features advanced technology and premium design. Perfect for everyday use.";
        }
        
        double price = Math.round((50.0 + random.nextDouble() * 1950.0) * 100.0) / 100.0;
        
        List<String> categories = new ArrayList<>();
        categories.add(CATEGORIES[random.nextInt(CATEGORIES.length)]);
        if (random.nextBoolean()) {
            String secondCategory = CATEGORIES[random.nextInt(CATEGORIES.length)];
            if (!categories.contains(secondCategory)) {
                categories.add(secondCategory);
            }
        }
        
        String imageUrl = "https://example.com/images/products/" + productCode + ".jpg";
        
        LocalDateTime now = LocalDateTime.now();
        
        ProductSearch product = new ProductSearch();
        product.setProductCode(productCode);
        product.setName(name);
        product.setBrand(brand);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(categories);
        product.setImageUrl(imageUrl);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        return product;
    }
}

