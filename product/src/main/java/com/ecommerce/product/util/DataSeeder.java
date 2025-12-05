package com.ecommerce.product.util;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() < 50000) {
            System.out.println("Seeding products...");
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 50000; i++) {
                Product p = new Product();
                p.setName("Product " + i);
                p.setDescription("Description for product " + i);
                p.setPrice(10.0 + (i % 100));
                products.add(p);

                if (i % 1000 == 0) {
                    productRepository.saveAll(products);
                    products.clear();
                    System.out.println("Seeded " + i + " products");
                }
            }
            if (!products.isEmpty()) {
                productRepository.saveAll(products);
            }
            System.out.println("Seeding complete.");
        }
    }
}
