package com.project.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.member.dto.AuthResponse;
import com.project.member.dto.LoginRequest;
import com.project.member.dto.RegisterRequest;
import com.project.member.entity.Member;
import com.project.member.repositories.MemberRepository;
import com.project.member.repositories.RevokedTokenRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Member API endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    private static final String BASE_URL = "/api/member";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULL_NAME = "Test User";

    @BeforeEach
    void setUp() {
        // Clean up test data before each test
        revokedTokenRepository.deleteAll();
        memberRepository.findByEmail(TEST_EMAIL).ifPresent(memberRepository::delete);
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFullName(TEST_FULL_NAME);

        // When & Then
        MvcResult result = mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Verify response
        String responseBody = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).isNotEmpty();

        // Verify user was saved in database
        Member savedMember = memberRepository.findByEmail(TEST_EMAIL).orElse(null);
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedMember.getFullName()).isEqualTo(TEST_FULL_NAME);
        assertThat(savedMember.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to register with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        // Given - Create a user first
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setEmail(TEST_EMAIL);
        firstRequest.setPassword(TEST_PASSWORD);
        firstRequest.setFullName(TEST_FULL_NAME);

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // When - Try to register again with same email
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setEmail(TEST_EMAIL);
        duplicateRequest.setPassword("differentPassword");
        duplicateRequest.setFullName("Different Name");

        // Then
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to register with invalid email format")
    void testRegisterInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword(TEST_PASSWORD);
        request.setFullName(TEST_FULL_NAME);

        // When & Then
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Given - Register a user first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName(TEST_FULL_NAME);

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // When - Login with same credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        // Then
        MvcResult result = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Verify token is returned
        String responseBody = result.getResponse().getContentAsString();
        AuthResponse response = objectMapper.readValue(responseBody, AuthResponse.class);
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).isNotEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Should fail to login with wrong password")
    void testLoginWrongPassword() throws Exception {
        // Given - Register a user first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName(TEST_FULL_NAME);

        mockMvc.perform(post(BASE_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // When - Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("wrongPassword");

        // Then
        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Should fail to login with non-existent email")
    void testLoginNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword(TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("Should logout successfully with valid token")
    void testLogoutSuccess() throws Exception {
        // Given - Register and get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName(TEST_FULL_NAME);

        MvcResult registerResult = mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String responseBody = registerResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        String token = authResponse.getToken();

        // When - Logout with token
        mockMvc.perform(post(BASE_URL + "/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Then - Verify token was revoked
        assertThat(revokedTokenRepository.count()).isGreaterThan(0);
    }

    @Test
    @Order(8)
    @DisplayName("Should handle logout without token gracefully")
    void testLogoutWithoutToken() throws Exception {
        // When & Then - Logout without Authorization header
        mockMvc.perform(post(BASE_URL + "/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle logout with invalid token format gracefully")
    void testLogoutInvalidTokenFormat() throws Exception {
        // When & Then - Logout with invalid token format
        mockMvc.perform(post(BASE_URL + "/logout")
                        .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(10)
    @DisplayName("Complete user flow: Register -> Login -> Logout")
    void testCompleteUserFlow() throws Exception {
        // Step 1: Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName(TEST_FULL_NAME);

        MvcResult registerResult = mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String registerToken = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                AuthResponse.class
        ).getToken();
        assertThat(registerToken).isNotNull();

        // Step 2: Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginToken = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        ).getToken();
        assertThat(loginToken).isNotNull();

        // Step 3: Logout
        mockMvc.perform(post(BASE_URL + "/logout")
                        .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isNoContent());

        // Verify token was revoked
        assertThat(revokedTokenRepository.count()).isGreaterThan(0);
    }
}
