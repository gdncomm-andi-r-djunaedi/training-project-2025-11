package com.customer.controller;

import com.customer.dto.request.LoginRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalAuthController.class)
class InternalAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerAuthRepository customerAuthRepository;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------
    // Test GET /internal/auth/customer (success)
    // -------------------------------------------------------
    @Test
    void testFindByEmail_ReturnsCustomerAuth() throws Exception {
        UUID custId = UUID.randomUUID();

        Customer customer = Customer.builder()
                .id(custId)
                .name("Test User")
                .email("test@gmail.com")
                .phone("12345")
                .address("Address")
                .createdAt(LocalDateTime.now())
                .build();

        CustomerAuth auth = CustomerAuth.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .email("test@gmail.com")
                .passwordHash("hashed-password")
                .build();

        Mockito.when(customerAuthRepository.findByEmailIgnoreCase("test@gmail.com"))
                .thenReturn(Optional.of(auth));

        mockMvc.perform(get("/internal/auth/customer")
                        .param("email", "test@gmail.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(custId.toString()))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // -------------------------------------------------------
    // Test GET /internal/auth/customer (not found)
    // -------------------------------------------------------
    @Test
    void testFindByEmail_NotFound() throws Exception {

        Mockito.when(customerAuthRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/internal/auth/customer")
                        .param("email", "notfound@mail.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // Test POST /internal/auth/login (success)
    // -------------------------------------------------------
    @Test
    void testLogin_Success() throws Exception {

        UUID customerId = UUID.randomUUID();

        CustomerResponse mockCustomer = new CustomerResponse(
                customerId,
                "test",
                "test@gmail.com",
                "12345",
                "Address"
        );

        Mockito.when(customerService.validateLogin("test@gmail.com", "123"))
                .thenReturn(mockCustomer);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("123");

        mockMvc.perform(post("/internal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }
}
