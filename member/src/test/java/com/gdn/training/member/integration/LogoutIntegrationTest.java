package com.gdn.training.member.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        Member member = new Member("testuser", "test@example.com", passwordEncoder.encode("password"));
        memberRepository.save(member);
    }

    @Test
    void logout_Success() throws Exception {
        // 1. Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
        MvcResult loginResult = mockMvc.perform(post("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // 2. Access protected resource with token (should succeed)
        // Assuming there is a protected endpoint, or we can use logout itself as a
        // protected endpoint test
        // Let's try to logout first
        mockMvc.perform(post("/api/members/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Verify lastLogout is set
        Member member = memberRepository.findByUsername("testuser").orElseThrow();
        assert (member.getLastLogout() != null);

        // 4. Access protected resource with blacklisted token (should fail)
        // We can try to logout again, or access another protected endpoint if
        // available.
        // Let's try logout again, it requires authentication.
        mockMvc.perform(post("/api/members/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
