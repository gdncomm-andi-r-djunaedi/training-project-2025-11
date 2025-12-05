package com.gdn.faurihakim.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Cart Service Integration Tests")
class CartIntegrationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        @DisplayName("Should load application context successfully")
        void testContextLoads() {
                // Assert that the context loads correctly with H2 database
                assertThat(applicationContext).isNotNull();
        }

        @Test
        @DisplayName("Should have CartController bean")
        void testCartControllerBean() {
                // Assert that CartController bean exists
                assertThat(applicationContext.containsBean("cartController")).isTrue();
        }
}
