package com.gdn.marketplace.product.config;

import com.gdn.marketplace.product.entity.Product;
import com.gdn.marketplace.product.repository.ProductRepository;
import com.gdn.marketplace.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 50000; i++) {
                products.add(new Product(null, "Product " + i, "Description for product " + i, new BigDecimal(i), "Category " + (i % 10)));
                if (i % 1000 == 0) {
                    service.saveProducts(products);
                    products.clear();
                    System.out.println("Seeded " + i + " products");
                }
            }
            if (!products.isEmpty()) {
                service.saveProducts(products);
            }
            System.out.println("Seeded 50000 products");
        }
    }
}
