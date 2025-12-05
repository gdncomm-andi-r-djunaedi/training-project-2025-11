package com.dev.onlineMarketplace.MemberService.service;

import com.dev.onlineMarketplace.MemberService.dto.*;
import com.dev.onlineMarketplace.MemberService.entity.MemberEntity;
import com.dev.onlineMarketplace.MemberService.exception.InvalidCredentialsException;
import com.dev.onlineMarketplace.MemberService.exception.MemberAlreadyExistsException;
import com.dev.onlineMarketplace.MemberService.exception.MemberNotFoundException;
import com.dev.onlineMarketplace.MemberService.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public MemberDTO register(RegisterRequestDTO request) {
        logger.info("Attempting to register user with email: {}", request.getEmail());

        // Check if email already exists
        if (memberRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new MemberAlreadyExistsException("User already registered");
        }

        // Create new member entity
        MemberEntity member = new MemberEntity();
        member.setUsername(request.getEmail()); // Using email as username
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save to database
        MemberEntity savedMember = memberRepository.save(member);
        logger.info("User registered successfully with ID: {}", savedMember.getId());

        // Convert to DTO
        return new MemberDTO(
                savedMember.getUsername(),
                savedMember.getEmail(),
                "********"); // Never expose actual password
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        logger.info("Login attempt for username: {}", request.getUsername());

        // Find member by username or email
        MemberEntity member = memberRepository.findByUsername(request.getUsername())
                .or(() -> memberRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found - {}", request.getUsername());
                    return new MemberNotFoundException("User not found");
                });

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            logger.warn("Login failed: Invalid credentials for username - {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        logger.info("Login successful for username: {}", request.getUsername());

        return new LoginResponseDTO(
                member.getUsername(),
                member.getEmail(),
                "Login successful"
        );
    }

    @Override
    @Transactional
    public void logout(String token) {
        logger.info("Logout successful");
        // Token generation removed - logout is now a no-op
        // In a real-world scenario without tokens, you might want to clear session data
        // or perform other cleanup operations here
    }

}
