package com.example.productservice;

import com.example.productservice.repository.ProductRepository;
import com.example.productservice.util.ProductDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/product_db" // Connects to Docker Mongo
})
class ManualDataSeederTest {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Run this test manually to seed the Docker database:
     * mvn test -Dtest=ManualDataSeederTest
     */
    @Test
    void seedDockerDatabase() {
        ProductDataSeeder.seed(productRepository);
    }
}
