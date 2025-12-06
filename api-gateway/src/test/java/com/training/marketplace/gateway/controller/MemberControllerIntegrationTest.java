package com.training.marketplace.gateway.controller;

import com.training.marketplace.gateway.dto.member.LoginRequestDTO;
import com.training.marketplace.gateway.dto.member.RegisterRequestDTO;
import com.training.marketplace.gateway.service.MemberClientService;
import com.training.marketplace.member.controller.modal.request.LoginResponse;
import com.training.marketplace.member.controller.modal.response.DefaultMemberResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberClientService memberClientService;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password");

        DefaultMemberResponse response = DefaultMemberResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Registration successful")
                .build();

        when(memberClientService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password");

        LoginResponse response = LoginResponse.newBuilder()
                .setMemberId("123")
                .setAuthToken("authToken")
                .setRefreshToken("refreshToken")
                .build();

        when(memberClientService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value("123"))
                .andExpect(jsonPath("$.authToken").value("authToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }
}
