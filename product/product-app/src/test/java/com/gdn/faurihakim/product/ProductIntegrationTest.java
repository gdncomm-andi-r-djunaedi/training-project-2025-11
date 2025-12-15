package com.gdn.faurihakim.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Product Service Integration Tests")
class ProductIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load application context successfully")
    void testContextLoads() {
        // Assert that the context loads correctly with Embedded MongoDB
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should have ProductController bean")
    void testProductControllerBean() {
        // Assert that ProductController bean exists
        assertThat(applicationContext.containsBean("productController")).isTrue();
    }
}
