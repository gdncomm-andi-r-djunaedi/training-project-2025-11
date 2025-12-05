package com.gdn.faurihakim.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("API Gateway Integration Tests")
class GatewayIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load application context successfully")
    void testContextLoads() {
        // Assert that the context loads correctly
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should have JwtUtil bean")
    void testJwtUtilBean() {
        // Assert that JwtUtil bean exists
        assertThat(applicationContext.containsBean("jwtUtil")).isTrue();
    }

    @Test
    @DisplayName("Should have AuthService bean")
    void testAuthServiceBean() {
        // Assert that AuthService bean exists
        assertThat(applicationContext.containsBean("authService")).isTrue();
    }

    @Test
    @DisplayName("Should have AuthenticationFilter bean")
    void testAuthenticationFilterBean() {
        // Assert that AuthenticationFilter bean exists
        assertThat(applicationContext.containsBean("authenticationFilter")).isTrue();
    }
}
