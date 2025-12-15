package com.training.member.service;

import com.training.member.dto.AuthResponse;
import com.training.member.dto.LoginRequest;
import com.training.member.dto.RegisterRequest;
import com.training.member.entity.Customer;
import com.training.member.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtService jwtService;

  public AuthResponse register(RegisterRequest request) {
    if (customerRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already exists");
    }
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    Customer customer = Customer.builder()
        .email(request.getEmail())
        .password(hashedPassword)
        .build();
    customerRepository.save(customer);
    String token = jwtService.generateToken(customer.getEmail());
    return AuthResponse.builder()
        .token(token)
        .email(customer.getEmail())
        .build();
  }

  public AuthResponse login(LoginRequest request) {
    Customer customer = customerRepository.findByEmail(request.getEmail())
        .filter(c -> passwordEncoder.matches(request.getPassword(), c.getPassword()))
        .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    String token = jwtService.generateToken(customer.getEmail());
    return AuthResponse.builder()
        .token(token)
        .email(customer.getEmail())
        .build();
  }
}
