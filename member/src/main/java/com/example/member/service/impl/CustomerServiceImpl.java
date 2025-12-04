package com.example.member.service.impl;

import com.example.member.dto.AuthResponse;
import com.example.member.dto.LoginRequest;
import com.example.member.dto.RegisterRequest;
import com.example.member.entity.Customer;
import com.example.member.repository.CustomerRepository;
import com.example.member.service.CustomerService;
import com.example.member.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if customer already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Customer with email " + request.getEmail() + " already exists");
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        // Hash password using Spring's PasswordEncoder
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save customer
        customer = customerRepository.save(customer);

        // Generate JWT token
        String token = jwtUtil.generateToken(customer.getEmail());

        return new AuthResponse(token, "Bearer", customer.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find customer by email
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Validate password using Spring's PasswordEncoder
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(customer.getEmail());

        return new AuthResponse(token, "Bearer", customer.getEmail());
    }
}
