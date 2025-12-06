package com.marketplace.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("New User"))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void register_DuplicateEmail_ReturnsConflict() throws Exception {
        // Create existing member
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        Member existingMember = Member.builder()
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Existing User")
                .roles(roles)
                .build();
        memberRepository.save(existingMember);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("Password123!");
        request.setFullName("New User");

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_WeakPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("weak");  // Too short, no uppercase, no digit, no special char
        request.setFullName("New User");

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_OptionalFullName_ReturnsCreated() throws Exception {
        // fullName is optional - registration should succeed without it
        RegisterRequest request = new RegisterRequest();
        request.setEmail("noname@example.com");
        request.setPassword("Password123!");
        // fullName is not set

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void validateCredentials_ValidCredentials_ReturnsOk() throws Exception {
        // Create member with known password
        String rawPassword = "Password123!";
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        Member member = Member.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName("Test User")
                .roles(roles)
                .build();
        memberRepository.save(member);

        ValidateCredentialsRequest request = ValidateCredentialsRequest.builder()
                .email("user@example.com")
                .password(rawPassword)
                .build();

        mockMvc.perform(post("/api/member/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles", hasItem("ROLE_USER")));
    }

    @Test
    void validateCredentials_InvalidPassword_ReturnsUnauthorized() throws Exception {
        // Create member
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        Member member = Member.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .fullName("Test User")
                .roles(roles)
                .build();
        memberRepository.save(member);

        ValidateCredentialsRequest request = ValidateCredentialsRequest.builder()
                .email("user@example.com")
                .password("WrongPassword!")
                .build();

        mockMvc.perform(post("/api/member/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void validateCredentials_NonExistentUser_ReturnsUnauthorized() throws Exception {
        ValidateCredentialsRequest request = ValidateCredentialsRequest.builder()
                .email("nonexistent@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/member/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void validateCredentials_MissingEmail_ReturnsBadRequest() throws Exception {
        ValidateCredentialsRequest request = ValidateCredentialsRequest.builder()
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/member/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateCredentials_MissingPassword_ReturnsBadRequest() throws Exception {
        ValidateCredentialsRequest request = ValidateCredentialsRequest.builder()
                .email("user@example.com")
                .build();

        mockMvc.perform(post("/api/member/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

