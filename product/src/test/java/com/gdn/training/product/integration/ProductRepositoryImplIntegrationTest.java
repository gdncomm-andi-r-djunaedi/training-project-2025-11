package com.gdn.training.product.integration;

import com.gdn.training.product.domain.model.Product;
import com.gdn.training.product.domain.repository.ProductRepository;
import com.gdn.training.product.infrastructure.persistence.entity.ProductEntity;
import com.gdn.training.product.infrastructure.persistence.jpa.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ProductRepositoryImplIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductJpaRepository productJpaRepository;

    @BeforeEach
    void setUp() {
        productJpaRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindProductById() {
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        ProductEntity entity = new ProductEntity();
        entity.setProductId(productId);
        entity.setSellerId(sellerId);
        entity.setName("Test Product");
        entity.setDescription("Test desc");
        entity.setCategory("electronics");
        entity.setPrice(BigDecimal.valueOf(999.99));
        entity.setStock(100);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(Instant.now());

        productJpaRepository.save(entity);

        // Act
        Optional<Product> result = productRepository.findById(productId);

        // Assert
        assertTrue(result.isPresent());
        Product product = result.get();
        assertEquals(productId.toString(), product.getProductId().toString());
        assertEquals("Test Product", product.getName());
        assertEquals(999.99, product.getPrice());
        assertEquals(100, product.getStock());
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        Optional<Product> result = productRepository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleMultipleProducts() {
        ProductEntity p1 = createTestEntity("Laptop", "tech", 1500.0, 5);
        ProductEntity p2 = createTestEntity("Mouse", "tech", 25.0, 100);
        ProductEntity p3 = createTestEntity("Keyboard", "tech", 75.0, 50);

        productJpaRepository.saveAll(List.of(p1, p2, p3));

        assertTrue(productRepository.findById(p1.getProductId()).isPresent());
        assertTrue(productRepository.findById(p2.getProductId()).isPresent());
        assertTrue(productRepository.findById(p3.getProductId()).isPresent());
    }

    @Test
    void shouldMapAllFieldsCorrectly() {
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant now = Instant.now();

        ProductEntity entity = new ProductEntity();
        entity.setProductId(productId);
        entity.setSellerId(sellerId);
        entity.setName("Premium Product");
        entity.setDescription("High-end item");
        entity.setCategory("premium");
        entity.setPrice(BigDecimal.valueOf(4999.99));
        entity.setStock(10);
        entity.setStatus("INACTIVE");
        entity.setCreatedAt(now);

        productJpaRepository.save(entity);

        Optional<Product> result = productRepository.findById(productId);
        assertTrue(result.isPresent());

        Product product = result.get();
        assertEquals(productId, product.getProductId());
        assertEquals(sellerId, product.getSellerId());
        assertEquals("Premium Product", product.getName());
        assertEquals("High-end item", product.getDescription());
        assertEquals("premium", product.getCategory());
        assertEquals(4999.99, product.getPrice());
        assertEquals(10, product.getStock());
        assertEquals(Product.Status.INACTIVE, product.getStatus());
        assertEquals(now, product.getCreatedAt());
    }

    private ProductEntity createTestEntity(String name, String category, double price, int stock) {
        ProductEntity entity = new ProductEntity();
        entity.setProductId(UUID.randomUUID());
        entity.setSellerId(UUID.randomUUID());
        entity.setName(name);
        entity.setDescription("Desc for " + name);
        entity.setCategory(category);
        entity.setPrice(BigDecimal.valueOf(price));
        entity.setStock(stock);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}