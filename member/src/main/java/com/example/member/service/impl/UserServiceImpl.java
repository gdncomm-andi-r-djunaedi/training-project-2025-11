package com.example.member.service.impl;

import com.example.member.dto.UserRequestDto;
import com.example.member.dto.UserResponseDTO;
import com.example.member.entity.User;
import com.example.member.exceptions.InvalidCredentialsException;
import com.example.member.exceptions.UserAlreadyExistsException;
import com.example.member.repository.UserRepository;
import com.example.member.security.JwtUtil;
import com.example.member.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public UserResponseDTO registerUser(UserRequestDto userRequestDto) {
        if(userRequestDto.getEmail() == null || userRequestDto.getEmail().isEmpty())
            throw new IllegalArgumentException("email can't be null or empty");
        userRepository.findByEmail(userRequestDto.getEmail())
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException(
                            "User with email " + userRequestDto.getEmail() + " already exists");
                });

        User user = new User();
        BeanUtils.copyProperties(userRequestDto, user, "password");
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));

        User savedUser = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        BeanUtils.copyProperties(savedUser, response, "userId", "password");

        return response;
    }

    @Override
    public String loginUser(com.example.member.dto.LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtUtil.generateToken(user);
    }

    @Override
    public UserResponseDTO getMemberProfile(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.error("user is not present with the id: " + userId);
                    return new RuntimeException("User not found");
                });

        UserResponseDTO response = new UserResponseDTO();
        BeanUtils.copyProperties(user, response, "password", "userId");
        return response;
    }
}
