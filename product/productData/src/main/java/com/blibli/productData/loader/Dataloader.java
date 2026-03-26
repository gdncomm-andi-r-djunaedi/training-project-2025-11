package com.blibli.productData.loader;


import com.blibli.productData.entity.Product;
import com.blibli.productData.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Dataloader implements CommandLineRunner {

    private final ProductRepository productRepository;

    public Dataloader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        long existingCount = productRepository.count();
        if (existingCount > 0) {
            System.out.println("Products already loaded. Skipping data population.");
            return;
        }

        System.out.println("Populating 50 products...");

        String[] brands = {"Samsung", "Apple", "Xiaomi", "OnePlus", "Sony", "LG", "Nokia", "Huawei", "Motorola", "Oppo"};
        Random random = new Random();

        List<Product> batch = new ArrayList<>();
        int batchSize = 500;

        for (int i = 1; i <= 5000; i++) {
            String prodId = "PRD-" + String.format("%06d", i);
            String brand = brands[random.nextInt(brands.length)];
            String name = brand + " Product " + i;
            String description = "Description for " + name;
            double price = 5000 + random.nextInt(50000);
            String imageUrl = "https://example.com/images/" + prodId + ".jpg";


            String[] categoryPool = {
                    "Electronics", "Mobile", "Appliances", "Fashion",
                    "Home", "Sports", "Beauty", "Computers",
                    "Kitchen", "Toys"};

            int categoryCount = 1 + random.nextInt(3);
            Set<String> categorySet = new HashSet<>();

            while (categorySet.size() < categoryCount) {
                categorySet.add(categoryPool[random.nextInt(categoryPool.length)]);
            }

            List<String> categories = new ArrayList<>(categorySet);


            Product product = new Product();
            product.setProductId(prodId);
            product.setName(name);
            product.setDescription(description);
            product.setBrand(brand);
            product.setPrice(price);
            product.setImageUrl(imageUrl);
            product.setCategories(categories);
            batch.add(product);

            if (i % batchSize == 0) {
                productRepository.saveAll(batch);
                batch.clear();
                System.out.println(i + " products inserted...");
            }
        }

        if (!batch.isEmpty()) {
            productRepository.saveAll(batch);
            System.out.println("Final batch inserted.");
        }

        System.out.println("5000 products inserted successfully!");
    }
}

