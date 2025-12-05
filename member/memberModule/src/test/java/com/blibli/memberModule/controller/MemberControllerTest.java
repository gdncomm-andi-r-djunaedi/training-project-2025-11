package com.blibli.memberModule.controller;

import com.blibli.memberModule.dto.LoginRequestDto;
import com.blibli.memberModule.dto.LoginResponseDto;
import com.blibli.memberModule.dto.MemberRequestDto;
import com.blibli.memberModule.dto.MemberResponseDto;
import com.blibli.memberModule.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberRequestDto memberRequestDto;
    private MemberResponseDto memberResponseDto;
    private LoginRequestDto loginRequestDto;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    void setUp() {
        memberRequestDto = new MemberRequestDto();
        memberRequestDto.setEmail("test@gmail.com");
        memberRequestDto.setPassword("password123");
        memberRequestDto.setName("Test User");
        memberRequestDto.setPhone("1234567890");

        memberResponseDto = new MemberResponseDto();
        memberResponseDto.setMemberId(1L);
        memberResponseDto.setEmail("test@gmail.com");
        memberResponseDto.setName("Test User");
        memberResponseDto.setPhone("1234567890");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@gmail.com");
        loginRequestDto.setPassword("password123");

        loginResponseDto = new LoginResponseDto();
        loginResponseDto.setMember(memberResponseDto);
    }

    @Test
    void testRegister_Success() throws Exception {
        when(memberService.register(any(MemberRequestDto.class))).thenReturn(memberResponseDto);

        mockMvc.perform(post("/api/members/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberRequestDto))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.memberId").value(1))
                .andExpect(jsonPath("$.value.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.value.name").value("Test User"));
    }

    @Test
    void testLogin_Success() throws Exception {
        when(memberService.login(any(LoginRequestDto.class))).thenReturn(loginResponseDto);

        mockMvc.perform(post("/api/members/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto))).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.member.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.value.member.memberId").value(1));
    }

    @Test
    void testLogout_Success() throws Exception {
        doNothing().when(memberService).logout(1L);

        mockMvc.perform(post("/api/members/logout").param("memberId", "1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value").value("Logout successful"));
    }
}

