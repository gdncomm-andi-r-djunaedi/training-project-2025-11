package com.gdn.training.product.repository;

import com.gdn.training.product.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByFilters_ShouldWork() {
        productRepository.findByFilters(null, "test", PageRequest.of(0, 10));
    }
}
