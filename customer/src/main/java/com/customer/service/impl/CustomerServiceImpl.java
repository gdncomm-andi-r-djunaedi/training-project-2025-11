package com.customer.service.impl;

import com.customer.dto.CreateCustomerRequest;
import com.customer.dto.LoginRequest;
import com.customer.dto.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.repository.CustomerRepository;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAuthRepository customerAuthRepository;

    @Override
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        CustomerAuth auth = CustomerAuth.builder()
                .customer(savedCustomer)
                .email(request.getEmail())
                .passwordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .lastLogin(null)
                .build();

        customerAuthRepository.save(auth);

        return savedCustomer;
    }

    @Override
    public Customer updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());

        return customerRepository.save(customer);
    }

    @Override
    public boolean login(LoginRequest request) {
        CustomerAuth auth = customerAuthRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        return BCrypt.checkpw(request.getPassword(), auth.getPasswordHash());
    }

    @Override
    public Page<Customer> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findAll(pageable);
    }

    @Override
    public void deleteCustomer(UUID id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getById(UUID id) {
        return customerRepository.getById(id);
    }

    @Override
    public Page<Customer> getAllCustomersByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
