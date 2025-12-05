package com.product.integration;

import com.product.entity.Product;
import com.product.repository.ProductRepository;
import com.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductIntegrationTest {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductRepository productRepository;

    private Product sample;

    @BeforeEach
    void setup() {
        sample = new Product();
        sample.setName("Laptop");
        sample.setDescription("Fast laptop");
        sample.setPrice(BigDecimal.valueOf(1500.0));
        sample.setCategory("Electronics");
        sample.setImageUrl("http://image");
        sample.setActive(true);
        sample.setCreatedAt(LocalDateTime.now());
        sample.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateProduct() {
        Product saved = productService.create(sample);

        assertNotNull(saved.getId());
        assertThat(saved.getName()).isEqualTo("Laptop");
        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void testGetById() {
        productRepository.save(sample);

        Product result = productService.getById(sample.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    @Test
    void testGetAll() {
        productRepository.save(sample);

        List<Product> all = productService.getAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void testUpdate() {
        productRepository.save(sample);

        Product updated = new Product();
        updated.setName("Updated Laptop");
        updated.setDescription("Updated Desc");
        updated.setPrice(BigDecimal.valueOf(2000.0));
        updated.setCategory("IT");
        updated.setImageUrl("http://new-image");
        updated.setActive(false);
        sample.setCreatedAt(LocalDateTime.now());
        sample.setUpdatedAt(LocalDateTime.now());

        Product result = productService.update(sample.getId(), updated);

        assertThat(result.getName()).isEqualTo("Updated Laptop");
        assertThat(result.isActive()).isFalse();
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(2000.0));
    }

    @Test
    void testDelete() {
        productRepository.save(sample);

        productService.delete(sample.getId());

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void testSearchProducts() {
        productRepository.save(sample);

        var page = productService.searchProducts("lap", 0, 10);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Laptop");
    }
}
