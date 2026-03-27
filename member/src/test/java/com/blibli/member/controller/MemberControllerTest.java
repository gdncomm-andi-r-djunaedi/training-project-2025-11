package com.blibli.member.controller;

import com.blibli.member.dto.LoginRequest;
import com.blibli.member.dto.MemberResponse;
import com.blibli.member.dto.RegisterRequest;
import com.blibli.member.exception.BadRequestException;
import com.blibli.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("Member Controller Tests")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Test
    @DisplayName("Should register member successfully")
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(MEMBER_ID.toString())
                .email(EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of("CUSTOMER"))
                .createdAt(LocalDateTime.now())
                .build();

        when(memberService.register(any(RegisterRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.firstName").value(FIRST_NAME));

        verify(memberService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when request validation fails")
    void register_Failure_ValidationError() throws Exception {
        // Given
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("invalid-email") // Invalid email format
                .password("short") // Too short password
                .firstName("") // Empty first name
                .build();

        // When/Then
        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should authenticate member successfully")
    void authenticate_Success() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        MemberResponse response = MemberResponse.builder()
                .id(MEMBER_ID.toString())
                .email(EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of("CUSTOMER"))
                .createdAt(LocalDateTime.now())
                .build();

        when(memberService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/members/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.id").value(MEMBER_ID.toString()));

        verify(memberService).authenticate(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when login request validation fails")
    void authenticate_Failure_ValidationError() throws Exception {
        // Given
        LoginRequest invalidRequest = LoginRequest.builder()
                .email("invalid-email") // Invalid email format
                .password("") // Empty password
                .build();

        // When/Then
        mockMvc.perform(post("/api/members/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get member by ID successfully")
    void getMember_Success() throws Exception {
        // Given
        MemberResponse response = MemberResponse.builder()
                .id(MEMBER_ID.toString())
                .email(EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of("CUSTOMER"))
                .createdAt(LocalDateTime.now())
                .build();

        when(memberService.getMemberById(eq(MEMBER_ID))).thenReturn(response);

        // When/Then
        mockMvc.perform(get("/api/members/{id}", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(MEMBER_ID.toString()))
                .andExpect(jsonPath("$.data.email").value(EMAIL));

        verify(memberService).getMemberById(eq(MEMBER_ID));
    }

    @Test
    @DisplayName("Should return 400 when invalid UUID format")
    void getMember_Failure_InvalidUUID() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/members/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle BadRequestException from service")
    void register_Failure_ServiceException() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();

        when(memberService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email already registered"));

        // When/Then
        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

