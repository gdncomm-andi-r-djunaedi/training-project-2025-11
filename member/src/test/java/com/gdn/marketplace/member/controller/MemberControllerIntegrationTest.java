package com.gdn.marketplace.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.marketplace.member.dto.LoginRequest;
import com.gdn.marketplace.member.dto.RegisterRequest;
import com.gdn.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void register_ShouldReturnMember() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("integrationUser");
        request.setPassword("password");
        request.setEmail("integration@example.com");

        mockMvc.perform(post("/api/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationUser"));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        // First register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginUser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("login@example.com");

        mockMvc.perform(post("/api/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginUser");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
