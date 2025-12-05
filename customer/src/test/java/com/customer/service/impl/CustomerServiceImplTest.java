package com.customer.service.impl;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAuthRepository customerAuthRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl service;

    private Customer mockCustomer;
    private CustomerAuth mockAuth;

    @BeforeEach
    void setUp() {

        mockCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Sylvia")
                .email("sylvia@mail.com")
                .phone("08123")
                .address("Gading Serpong")
                .createdAt(LocalDateTime.now())
                .build();

        mockAuth = CustomerAuth.builder()
                .id(UUID.randomUUID())
                .customer(mockCustomer)
                .email(mockCustomer.getEmail())
                .passwordHash("hashed")
                .lastLogin(null)
                .build();
    }

    // ----------------------------
    // Test createCustomer
    // ----------------------------
    @Test
    void testCreateCustomer() {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setName("Sylvia");
        req.setEmail("sylvia@mail.com");
        req.setPhone("08123");
        req.setAddress("Gading Serpong");
        req.setPassword("mypassword");

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(mockCustomer);

        when(customerAuthRepository.save(any(CustomerAuth.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Customer saved = service.createCustomer(req);

        assertNotNull(saved);
        assertEquals("Sylvia", saved.getName());
        verify(customerRepository).save(any(Customer.class));
        verify(customerAuthRepository).save(any(CustomerAuth.class));
    }

    // ----------------------------
    // Test updateCustomer (success)
    // ----------------------------
    @Test
    void testUpdateCustomer() {
        UUID id = mockCustomer.getId();

        UpdateCustomerRequest req = new UpdateCustomerRequest();
        req.setName("Updated Name");
        req.setPhone("08122");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(mockCustomer));

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(mockCustomer);

        Customer updated = service.updateCustomer(id, req);

        assertEquals("Updated Name", updated.getName());
        assertEquals("08122", updated.getPhone());
    }

    // ----------------------------
    // Test updateCustomer (not found)
    // ----------------------------
    @Test
    void testUpdateCustomer_NotFound() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updateCustomer(id, new UpdateCustomerRequest()));
    }

    // ----------------------------
    // Test getAllCustomers
    // ----------------------------
    @Test
    void testGetAllCustomers() {
        Page<Customer> page = new PageImpl<>(List.of(mockCustomer));

        when(customerRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = service.getAllCustomers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Sylvia", result.getContent().get(0).getName());
    }

    // ----------------------------
    // Test deleteCustomer (exists)
    // ----------------------------
    @Test
    void testDeleteCustomer_Exists() {
        UUID id = mockCustomer.getId();

        when(customerRepository.existsById(id))
                .thenReturn(true);

        service.deleteCustomer(id);

        verify(customerAuthRepository).deleteByCustomerId(id);
        verify(customerRepository).deleteById(id);
    }

    // ----------------------------
    // Test deleteCustomer (not exists)
    // ----------------------------
    @Test
    void testDeleteCustomer_NotExists() {
        UUID id = UUID.randomUUID();

        when(customerRepository.existsById(id))
                .thenReturn(false);

        service.deleteCustomer(id);

        verify(customerAuthRepository, never()).deleteByCustomerId(any());
        verify(customerRepository, never()).deleteById(any());
    }

    // ----------------------------
    // Test getById
    // ----------------------------
    @Test
    void testGetById() {
        UUID id = mockCustomer.getId();

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(mockCustomer));

        CustomerResponse response = service.getById(id);

        assertEquals(id, response.getId());
        assertEquals("Sylvia", response.getName());
    }

    // ----------------------------
    // Test getById (not found)
    // ----------------------------
    @Test
    void testGetById_NotFound() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(id));
    }

    // ----------------------------
    // Test getAllCustomersByName
    // ----------------------------
    @Test
    void testGetAllCustomersByName() {
        Page<Customer> page = new PageImpl<>(List.of(mockCustomer));

        when(customerRepository.findByNameContainingIgnoreCase(eq("syl"), any(Pageable.class)))
                .thenReturn(page);

        Page<Customer> result = service.getAllCustomersByName("syl", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // ----------------------------
    // Test exists
    // ----------------------------
    @Test
    void testExists() {
        UUID id = mockCustomer.getId();

        when(customerRepository.existsById(id))
                .thenReturn(true);

        assertTrue(service.exists(id));
    }

    // ----------------------------
    // Test validateLogin (success)
    // ----------------------------
    @Test
    void testValidateLogin_Success() {
        String rawPassword = "mypassword";

        when(customerAuthRepository.findByEmailIgnoreCase(mockCustomer.getEmail()))
                .thenReturn(Optional.of(mockAuth));

        when(passwordEncoder.matches(rawPassword, "hashed"))
                .thenReturn(true);

        CustomerResponse response = service.validateLogin(mockCustomer.getEmail(), rawPassword);

        assertEquals(mockCustomer.getId(), response.getId());
        verify(customerAuthRepository).save(any(CustomerAuth.class)); // last login update
    }

    // ----------------------------
    // Test validateLogin (invalid password)
    // ----------------------------
    @Test
    void testValidateLogin_InvalidPassword() {
        when(customerAuthRepository.findByEmailIgnoreCase(mockCustomer.getEmail()))
                .thenReturn(Optional.of(mockAuth));

        when(passwordEncoder.matches("wrong", "hashed"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                service.validateLogin(mockCustomer.getEmail(), "wrong"));
    }

    // ----------------------------
    // Test validateLogin (email not found)
    // ----------------------------
    @Test
    void testValidateLogin_EmailNotFound() {
        when(customerAuthRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.validateLogin("none@mail.com", "pw"));
    }
}
