package com.blibli.AuthService.auth;

import com.blibli.AuthService.controller.AuthController;
import com.blibli.AuthService.dto.LoginRequestDto;
import com.blibli.AuthService.dto.LoginResponseDto;
import com.blibli.AuthService.dto.RegisterRequestDto;
import com.blibli.AuthService.service.AuthService;
import com.blibli.AuthService.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void register_shouldReturn201() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user1")
                .password("password")
                .email("user@test.com")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"));

        verify(userService, times(1)).register(any(RegisterRequestDto.class));
    }


    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequestDto request = new LoginRequestDto("user1", "password");

        LoginResponseDto response = LoginResponseDto.builder()
                .token("jwt-token")
                .userId("1")
                .expiresIn(123456L)
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));

        verify(authService).login(any());
    }


    @Test
    void logout_shouldReturn400_whenNoAuthHeader() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldRevokeToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk());

        verify(authService).revokeToken("test-token");
    }
}
