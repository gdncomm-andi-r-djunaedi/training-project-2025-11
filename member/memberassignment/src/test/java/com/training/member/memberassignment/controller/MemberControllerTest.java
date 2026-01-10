package com.training.member.memberassignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.member.memberassignment.dto.InputDTO;
import com.training.member.memberassignment.dto.OutputDTO;
import com.training.member.memberassignment.exception.GlobalExceptionHandler;
import com.training.member.memberassignment.exception.MemberException;
import com.training.member.memberassignment.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@ContextConfiguration(classes = { MemberController.class, GlobalExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private InputDTO validRegistrationInput;
    private InputDTO validLoginInput;
    private OutputDTO loginOutput;

    @BeforeEach
    void setUp() {
        validRegistrationInput = InputDTO.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        validLoginInput = InputDTO.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        loginOutput = OutputDTO.builder()
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("POST /member/register - Should successfully register new member")
    void registerMember_Success() throws Exception {
        doNothing().when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"));

        verify(memberService, times(1)).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/register - Should return conflict when email already exists")
    void registerMember_EmailAlreadyExists() throws Exception {
        doThrow(MemberException.emailAlreadyExists("test@example.com"))
                .when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationInput)))
                .andExpect(status().isConflict());

        verify(memberService, times(1)).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/register - Should return bad request for invalid payload")
    void registerMember_InvalidPayload() throws Exception {
        doThrow(MemberException.invalidPayload())
                .when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationInput)))
                .andExpect(status().isBadRequest());

        verify(memberService, times(1)).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/register - Should handle missing email field")
    void registerMember_MissingEmail() throws Exception {
        InputDTO inputWithoutEmail = InputDTO.builder()
                .password("Password123")
                .build();

        doThrow(MemberException.invalidPayload())
                .when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputWithoutEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /member/register - Should handle missing password field")
    void registerMember_MissingPassword() throws Exception {
        InputDTO inputWithoutPassword = InputDTO.builder()
                .email("test@example.com")
                .build();

        doThrow(MemberException.invalidPayload())
                .when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputWithoutPassword)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /member/register - Should reject malformed JSON")
    void registerMember_MalformedJson() throws Exception {
        String malformedJson = "{\"email\": \"test@example.com\", \"password\": }";

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should successfully login with valid credentials")
    void loginMember_Success() throws Exception {
        when(memberService.login(any(InputDTO.class))).thenReturn(loginOutput);

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(memberService, times(1)).login(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should return unauthorized for invalid credentials")
    void loginMember_InvalidCredentials() throws Exception {
        when(memberService.login(any(InputDTO.class)))
                .thenThrow(MemberException.invalidCredentials());

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginInput)))
                .andExpect(status().isUnauthorized());

        verify(memberService, times(1)).login(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should handle non-existent user")
    void loginMember_UserNotFound() throws Exception {
        when(memberService.login(any(InputDTO.class)))
                .thenThrow(MemberException.invalidCredentials());

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginInput)))
                .andExpect(status().isUnauthorized());

        verify(memberService, times(1)).login(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should reject empty request body")
    void loginMember_EmptyRequestBody() throws Exception {
        when(memberService.login(any(InputDTO.class)))
                .thenThrow(MemberException.invalidPayload());

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /member/login - Should handle missing content type")
    void loginMember_MissingContentType() throws Exception {
        mockMvc.perform(post("/member/login")
                .content(objectMapper.writeValueAsString(validLoginInput)))
                .andExpect(status().isUnsupportedMediaType());

        verify(memberService, never()).login(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should return correct response structure")
    void loginMember_CorrectResponseStructure() throws Exception {
        OutputDTO expectedOutput = OutputDTO.builder()
                .email("alice@example.com")
                .build();

        when(memberService.login(any(InputDTO.class))).thenReturn(expectedOutput);

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("POST /member/register - Should accept valid email formats")
    void registerMember_ValidEmailFormats() throws Exception {
        InputDTO validEmailInput = InputDTO.builder()
                .email("user.name+tag@example.co.uk")
                .password("SecurePass123")
                .build();

        doNothing().when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmailInput)))
                .andExpect(status().isOk());

        verify(memberService, times(1)).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/register - Should process request with special characters in password")
    void registerMember_SpecialCharactersInPassword() throws Exception {
        InputDTO specialCharInput = InputDTO.builder()
                .email("test@example.com")
                .password("P@ssw0rd!#$%")
                .build();

        doNothing().when(memberService).register(any(InputDTO.class));

        mockMvc.perform(post("/member/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialCharInput)))
                .andExpect(status().isOk());

        verify(memberService, times(1)).register(any(InputDTO.class));
    }

    @Test
    @DisplayName("POST /member/login - Should handle case-sensitive email")
    void loginMember_CaseSensitiveEmail() throws Exception {
        InputDTO upperCaseEmailInput = InputDTO.builder()
                .email("Test@Example.COM")
                .password("Password123")
                .build();

        when(memberService.login(any(InputDTO.class))).thenReturn(loginOutput);

        mockMvc.perform(post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upperCaseEmailInput)))
                .andExpect(status().isOk());

        verify(memberService, times(1)).login(any(InputDTO.class));
    }
}
