package com.blublu.product.util;

import com.blublu.product.document.Products;
import com.blublu.product.properties.ProductProperties;
import com.blublu.product.repository.ProductsRepository;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class FakeDataGenerator {

  private final Faker faker = new Faker();
  @Autowired
  private ProductsRepository productsRepository;
  @Autowired
  private ProductProperties productProperties;

  @PostConstruct
  public void generateData() {
    if (Boolean.parseBoolean(productProperties.getFlag().get("auto-generate-data"))) {
      long count = Integer.parseInt(productProperties.getFlag().getOrDefault("auto-generate-data-count", "10"));
      generateRealisticProduct(count);
    }
  }

  private void generateRealisticProduct(long count) {
    List<Products> products = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      BigDecimal price = new BigDecimal(faker.commerce().price(10000, 10000000));
      Products product = Products.builder()
          .price(price)
          .name(faker.commerce().productName())
          .originalPrice(price)
          .description(faker.lorem().characters(20, 100, true))
          .skuCode("MTA-" + String.format("%05d", i) + "-00001")
          .categories(new ArrayList<>(Arrays.asList(faker.commerce().department(),
              faker.commerce().department(),
              faker.commerce().department())))
          .build();
      products.add(product);
    }

    try {
      productsRepository.saveAll(products);
    } catch (Exception e) {
      log.info("Fail to generate {} data due to duplicate", count);
    }
  }
}
