package com.blibli.gdn.productService.config;

import com.blibli.gdn.productService.model.Product;
import com.blibli.gdn.productService.model.Variant;
import com.blibli.gdn.productService.repository.ProductRepository;
import com.blibli.gdn.productService.service.ProductIndexingService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    
    @Autowired(required = false)
    private ProductIndexingService productIndexingService;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Faker faker = new Faker();
            List<Product> batch = new ArrayList<>();
            java.util.Set<String> usedProductIds = new java.util.HashSet<>();
            for (int i = 0; i < 50000; i++) {
                // Ensure unique productId by using sequential number + random suffix
                String productId;
                int attempts = 0;
                do {
                    // Use sequential number to ensure uniqueness
                    String baseId = "SHT-60001-" + String.format("%06d", i);
                    String randomSuffix = String.valueOf(faker.number().numberBetween(100, 999));
                    productId = baseId + "-" + randomSuffix;
                    attempts++;
                    if (attempts > 10) {
                        // Fallback: use timestamp + random if still duplicate
                        productId = "SHT-60001-" + System.currentTimeMillis() + "-" + faker.number().numberBetween(1000, 9999);
                        break;
                    }
                } while (usedProductIds.contains(productId));
                usedProductIds.add(productId);
                String productName = faker.commerce().productName();

                int variantCount = faker.number().numberBetween(2, 4);
                List<Variant> variants = new ArrayList<>();
                for (int v = 0; v < variantCount; v++) {
                    String sku = productId + "-" + String.format("%05d", v);
                    variants.add(Variant.builder()
                            .sku(sku)
                            .size(faker.options().option("S", "M", "L", "XL"))
                            .color(faker.color().name())
                            .price(Double.parseDouble(faker.commerce().price().replace(",", ".")))
                            .stock(faker.number().numberBetween(1, 100))
                            .build());
                }

                Product product = Product.builder()
                        .productId(productId)
                        .name(productName)
                        .description(faker.lorem().sentence())
                        .category(faker.commerce().department())
                        .brand(faker.company().name())
                        .tags(Collections.singletonList(faker.commerce().material()))
                        .variants(variants)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                batch.add(product);

                if (i % 1000 == 0) {
                    productRepository.saveAll(batch);
                    batch.clear();
                    System.out.println("Seeded " + i + " products...");
                }
            }
            if (!batch.isEmpty()) {
                productRepository.saveAll(batch);
            }
            System.out.println("Seeding Complete! 50,000 products added.");
            if (productIndexingService != null) {
                System.out.println("Starting Elasticsearch indexing...");
                try {
                    productIndexingService.reindexAllProducts();
                    System.out.println("Elasticsearch indexing complete!");
                } catch (Exception e) {
                    System.out.println("Elasticsearch indexing failed (Elasticsearch may not be running): " + e.getMessage());
                }
            } else {
                System.out.println("Elasticsearch indexing skipped (Elasticsearch not enabled)");
            }
        }
    }
}
