package com.gdn.training.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.training.member.mapper.RegistrationMapper;
import com.gdn.training.member.model.entity.User;
import com.gdn.training.member.model.request.RegistrationRequest;
import com.gdn.training.member.repository.UserRepository;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationMapper registrationMapper;

    @Transactional
    public User register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = registrationMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
