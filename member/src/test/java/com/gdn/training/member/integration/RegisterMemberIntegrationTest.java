package com.gdn.training.member.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.member.dto.RegisterMemberRequest;
import com.gdn.training.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RegisterMemberIntegrationTest {

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
    void register_HappyFlow() throws Exception {
        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setUsername("integrationUser");
        request.setEmail("integration@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"));

        assertTrue(memberRepository.existsByUsername("integrationUser"));
        assertTrue(memberRepository.existsByEmail("integration@example.com"));
    }
}
