package com.sc.memberservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.memberservice.config.JwtAuthFilter;
import com.sc.memberservice.dto.LoginDto;
import com.sc.memberservice.dto.RegisterDto;
import com.sc.memberservice.service.MemberService;
import com.sc.memberservice.utils.JwtUtil;
import org.antlr.v4.runtime.misc.Pair;
import com.sc.utilsservice.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetails userDetails;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister() throws Exception {
        RegisterDto dto = new RegisterDto("Soumya", "abc@mail.com", "password");

        Mockito.when(memberService.register(ArgumentMatchers.any(RegisterDto.class)))
                .thenReturn(ApiResponse.success("Member registered successfully."));

        mockMvc.perform(MockMvcRequestBuilders.post("/members/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void testLogin() throws Exception {
        LoginDto dto = new LoginDto("abc@mail.com", "password");

        Pair<String, ApiResponse<String>> response =
                new Pair<>("mocktoken123", ApiResponse.success("Login ok"));

        Mockito.when(memberService.login(ArgumentMatchers.any(LoginDto.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/members/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    void testValidateToken() throws Exception {
        Mockito.when(jwtUtil.isTokenValid("abc123", userDetails)).thenReturn(true);
        Mockito.when(jwtUtil.extractSubject("abc123")).thenReturn("123:user@mail.com");

        mockMvc.perform(MockMvcRequestBuilders.get("/members/validate")
                        .with(csrf())
                        .header("Authorization", "Bearer abc123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }
}
