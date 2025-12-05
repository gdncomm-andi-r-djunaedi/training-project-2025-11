package com.blibli.AuthService.service.impl;

import com.blibli.AuthService.dto.RegisterRequestDto;
import com.blibli.AuthService.entity.UserEntity;
import com.blibli.AuthService.exceptions.BadRequestException;
import com.blibli.AuthService.repository.UserRepository;
import com.blibli.AuthService.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity register(RegisterRequestDto req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent() || userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("username or email already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        return userRepository.save(user);
    }
}
