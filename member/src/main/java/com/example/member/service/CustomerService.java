package com.example.member.service;

import com.example.member.dto.AuthResponse;
import com.example.member.dto.LoginRequest;
import com.example.member.dto.RegisterRequest;
import com.example.member.entity.Customer;
import com.example.member.repository.CustomerRepository;
import com.example.member.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

