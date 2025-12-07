package com.example.memberservice.service;

import com.example.memberservice.dto.AuthDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.exception.DuplicateUserException;
import com.example.memberservice.exception.InvalidPasswordException;
import com.example.memberservice.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String saveUser(AuthDto.RegisterRequest credential) {
        // Check if user already exists
        if (repository.findByUsername(credential.getUsername()).isPresent()) {
            throw new DuplicateUserException("Username already exists: " + credential.getUsername());
        }
        if (repository.findByEmail(credential.getEmail()).isPresent()) {
            throw new DuplicateUserException("Email already exists: " + credential.getEmail());
        }

        Member member = new Member();
        member.setUsername(credential.getUsername());
        member.setPassword(passwordEncoder.encode(credential.getPassword()));
        member.setEmail(credential.getEmail());
        repository.save(member);
        return "User added to the system";
    }

    public AuthDto.MemberValidationResponse validateUser(AuthDto.LoginRequest loginRequest) {
        Member member = repository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), member.getPassword());
        if (!matches) {
            throw new InvalidPasswordException("Invalid password");
        }

        return new AuthDto.MemberValidationResponse(member.getId(), member.getUsername());
    }

    public String generateToken(String username, Long userId) {
        return jwtService.generateToken(username, String.valueOf(userId));
    }
}
