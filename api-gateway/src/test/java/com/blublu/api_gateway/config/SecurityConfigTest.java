package com.blublu.api_gateway.config;

import com.blublu.api_gateway.config.filter.JwtAuthenticationFilter;
import com.blublu.api_gateway.config.filter.LoginResponseFilter;
import com.blublu.api_gateway.config.filter.LogoutResponseFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private LoginResponseFilter loginResponseFilter;

    @MockBean
    private LogoutResponseFilter logoutResponseFilter;

    @Test
    void testSecurityWebFilterChainBean() {
        SecurityWebFilterChain filterChain = context.getBean(SecurityWebFilterChain.class);
        assertNotNull(filterChain);
    }

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        assertNotNull(passwordEncoder);
    }
}
