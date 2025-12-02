package com.gdn.training.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

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
    void registerEndpointPersistsMember() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Bob");
        request.setEmail("bob@example.com");
        request.setPassword("Secret123!");

        mockMvc.perform(post("/internal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        Member saved = memberRepository.findByEmail("bob@example.com").orElseThrow();
        // password stored encoded and role is default
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("Secret123!", saved.getPassword())).isTrue();
    }

    @Test
    void validateCredentialsEndpointReturnsUserInfo() throws Exception {
        Member member = Member.builder()
                .name("Carol")
                .email("carol@example.com")
                .password(passwordEncoder.encode("Secret123!"))
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        LoginRequest request = new LoginRequest();
        request.setEmail("carol@example.com");
        request.setPassword("Secret123!");

        mockMvc.perform(post("/internal/auth/validate-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is("carol@example.com")))
                .andExpect(jsonPath("$.message", is("Credentials validated")));
    }
}

