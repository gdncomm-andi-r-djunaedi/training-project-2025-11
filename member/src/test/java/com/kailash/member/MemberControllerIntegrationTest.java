package com.kailash.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kailash.member.dto.LoginRequest;
import com.kailash.member.dto.RegisterRequest;
import com.kailash.member.entity.Member;
import com.kailash.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        memberRepository.deleteAll();
    }

    @Test
    void testRegister() throws Exception {

        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@mail.com");
        req.setPassword("pass123");
        req.setFullName("Test User");
        req.setPhone("9876543210");

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@mail.com"));
    }

    @Test
    void testLogin() throws Exception {

        // Insert user manually (service encodes password)
        Member m = Member.builder()
                .email("login@mail.com")
                .passwordHash(encoder.encode("pass123"))
                .fullName("Login User")
                .phone("1112223333")
                .createdAt(Instant.now())
                .build();
        memberRepository.save(m);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("login@mail.com");
        loginReq.setPassword("pass123");

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("login@mail.com"));
    }

    @Test
    void testGetMemberById() throws Exception {

        Member m = Member.builder()
                .email("abc@mail.com")
                .passwordHash("xxx")
                .fullName("ABC User")
                .phone("1111111111")
                .createdAt(Instant.now())
                .build();

        Member saved = memberRepository.save(m);

        mockMvc.perform(get("/members/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("abc@mail.com"));
    }

    @Test
    void testGetMe() throws Exception {

        Member m = Member.builder()
                .email("me@mail.com")
                .passwordHash("xxx")
                .fullName("Me User")
                .phone("1111222233")
                .createdAt(Instant.now())
                .build();

        Member saved = memberRepository.save(m);

        mockMvc.perform(get("/members/me")
                        .header("X-User-Id", saved.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@mail.com"));
    }
}
