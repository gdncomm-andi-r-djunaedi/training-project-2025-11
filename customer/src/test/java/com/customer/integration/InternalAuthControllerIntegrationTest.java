package com.customer.integration;

import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class InternalAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAuthRepository authRepository;

    @Test
    void testFindByEmail() throws Exception {
        Customer c = customerRepository.save(
                Customer.builder()
                        .name("Test")
                        .email("test@example.com")
                        .phone("123")
                        .address("Street")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        authRepository.save(
                CustomerAuth.builder()
                        .customer(c)
                        .email("test@example.com")
                        .passwordHash("hashed")
                        .build()
        );

        mockMvc.perform(get("/internal/auth/customer")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());
    }
}


