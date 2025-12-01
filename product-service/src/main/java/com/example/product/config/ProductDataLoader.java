package com.example.product.config;

import com.example.product.domain.Product;
import com.example.product.model.ProductTag;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class ProductDataLoader {

  private static final Logger log = LoggerFactory.getLogger(ProductDataLoader.class);

  private final ProductRepository repo;

  @Bean
  CommandLineRunner initProducts() {
    return args -> {
      if (repo.count() > 0)
        return;

      log.info("Seeding 50,000 demo products");
      Random rnd = new Random(42);

      ProductTag[] allTags = ProductTag.values();

      for (int i = 1; i <= 50000; i++) {
        Product p = Product.builder()
            .name("Product " + i)
            .description("Demo product " + i)
            .price(Math.round((10 + rnd.nextDouble() * 990) * 100.0) / 100.0)
            .categories(List.of("Category-" + (i % 10), "SubCategory-" + (i % 5)))
            .tags(List.of(allTags[i % allTags.length]))
            .createdAt(Instant.now())
            .build();

        repo.save(p);
      }

      log.info("Finished seeding products");
    };
  }
}