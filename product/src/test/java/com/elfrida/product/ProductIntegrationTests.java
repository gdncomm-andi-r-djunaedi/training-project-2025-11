package com.elfrida.product;

import com.elfrida.product.model.Product;
import com.elfrida.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTests {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0")
            .withReuse(true);

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        // Seed 30 products, 20 of them containing "shoe"
        IntStream.range(0, 20).forEach(i -> {
            Product p = new Product();
            p.setName("Running Shoe " + i);
            p.setDescription("Nice shoe " + i);
            p.setPrice(BigDecimal.TEN);
            p.setCategory("shoes");
            productRepository.save(p);
        });
        IntStream.range(0, 10).forEach(i -> {
            Product p = new Product();
            p.setName("Hat " + i);
            p.setDescription("Cool hat " + i);
            p.setPrice(BigDecimal.ONE);
            p.setCategory("hats");
            productRepository.save(p);
        });
    }

    @Test
    void searchProducts_shouldReturnPagedResults() {
        String base = "http://localhost:" + port;

        var response = restTemplate.getForEntity(
                base + "/products/search?name=shoe&page=0&size=5",
                String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Running Shoe");
    }

    @Test
    void getAllProducts_shouldReturnPagedList() {
        String base = "http://localhost:" + port;

        // page=0,size=10 hanya berisi 10 item pertama (semua "Running Shoe ...")
        var response = restTemplate.getForEntity(
                base + "/products?page=0&size=10",
                String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Running Shoe 0");

        // Ambil page berikutnya untuk memastikan data lain juga tersedia
        var secondPage = restTemplate.getForEntity(
                base + "/products?page=2&size=10",
                String.class);
        assertThat(secondPage.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(secondPage.getBody()).contains("Hat");
    }
}
