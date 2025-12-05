package com.example.member.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.member.repository.UserRepository;
import com.example.member.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new RuntimeException("Invalid credentials");
        return user;
    }
}
