package com.gdn.training.member.controller;

import com.gdn.training.member.entity.User;
import com.gdn.training.member.repository.UserRepository;
import com.gdn.training.member.dto.LoginResponse;
import com.gdn.training.member.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @GetMapping("/{username}")
    public User getProfile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping
    public List<User> getAllMembers() {
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody com.gdn.training.member.dto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername());
            return new LoginResponse(token, user.getUsername());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
