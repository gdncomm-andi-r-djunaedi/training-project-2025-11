package com.example.member.service;

import com.example.member.dto.*;
import com.example.member.entity.User;
import com.example.member.exception.InvalidCredentialsException;
import com.example.member.exception.UserAlreadyExistsException;
import com.example.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUserMail(request.getUserMail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getUserMail());
        }
        if (userRepository.existsByUserPhoneNumber(request.getUserPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists: " + request.getUserPhoneNumber());
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUserMail(request.getUserMail());
        user.setUsername(request.getUsername());
        user.setPassword(hashedPassword);
        user.setUserPhoneNumber(request.getUserPhoneNumber());
        user = userRepository.save(user);
        log.info("User registered successfully with username: {}", user.getUsername());
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("User registered successfully");
        return messageResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserMail(request.getUserEmail()).orElse(null);
        if (user==null) {
            throw new InvalidCredentialsException("Invalid email");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserId(user.getUserId());
        authResponse.setUserName(user.getUsername());
        authResponse.setEmail(user.getUserMail());
        return authResponse;
    }

}

