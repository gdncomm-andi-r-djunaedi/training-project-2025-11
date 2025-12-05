package com.blublu.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.blublu.member.interfaces.AuthenticationService;
import com.blublu.member.model.request.LoginRequest;
import com.blublu.member.model.request.SignUpRequest;
import com.blublu.member.service.MemberServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    MemberController memberController;

    @Mock
    MemberServiceImpl memberService;

    @Mock
    AuthenticationService authenticationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testUser")
                .password("password")
                .build();

        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());

        when(authenticationService.authenticateUser(any(LoginRequest.class))).thenReturn(userDetails);

        mockMvc.perform(post("/api/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void testSignUp_Success() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("newUser")
                .password("password")
                .build();

        doNothing().when(memberService).signUp(any(SignUpRequest.class));

        mockMvc.perform(post("/api/member/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/member/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
