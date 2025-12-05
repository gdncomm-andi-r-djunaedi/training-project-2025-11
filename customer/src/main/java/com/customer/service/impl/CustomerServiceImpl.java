package com.customer.service.impl;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.entity.CustomerAuth;
import com.customer.repository.CustomerAuthRepository;
import com.customer.repository.CustomerRepository;
import com.customer.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAuthRepository customerAuthRepository;
    private final PasswordEncoder passwordEncoder;

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
    public Page<Customer> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public void deleteCustomer(UUID id) {

        // If customer does not exist → do nothing
        if (!customerRepository.existsById(id)) {
            return;
        }
        customerAuthRepository.deleteByCustomerId(id);
        customerRepository.deleteById(id);
    }

    @Override
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }

    @Override
    public Page<Customer> getAllCustomersByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public boolean exists(UUID id) {
        return customerRepository.existsById(id);
    }

    @Override
    public CustomerResponse validateLogin(String email, String password) {

        CustomerAuth auth = customerAuthRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Validate password
        if (!passwordEncoder.matches(password, auth.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Update last login if you want
        auth.setLastLogin(LocalDateTime.now());
        customerAuthRepository.save(auth);

        Customer customer = auth.getCustomer();

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}

