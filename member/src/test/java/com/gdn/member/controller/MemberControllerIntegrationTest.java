package com.gdn.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.member.controller.webmodel.request.LoginMemberRequest;
import com.gdn.member.controller.webmodel.request.RegisterMemberRequest;
import com.gdn.member.entity.Member;
import com.gdn.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    memberRepository.deleteAll();
  }

  @Nested
  @DisplayName("POST /members/register")
  class RegisterTests {

    @Test
    @DisplayName("Should register member successfully")
    void shouldRegisterMemberSuccessfully() throws Exception {
      // Given
      RegisterMemberRequest request = new RegisterMemberRequest();
      request.setUsername("testuser");
      request.setPassword("Password123@");
      request.setName("Test User");
      request.setAddress("123 Test Street");

      // When
      ResultActions result = mockMvc.perform(post("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").exists())
          .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Should fail when username is blank")
    void shouldFailWhenUsernameIsBlank() throws Exception {
      // Given
      RegisterMemberRequest request = new RegisterMemberRequest();
      request.setUsername("");
      request.setPassword("Password123@");
      request.setName("Test User");
      request.setAddress("123 Test Street");

      // When
      ResultActions result = mockMvc.perform(post("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail when password is too weak")
    void shouldFailWhenPasswordIsTooWeak() throws Exception {
      // Given
      RegisterMemberRequest request = new RegisterMemberRequest();
      request.setUsername("testuser");
      request.setPassword("weakpass"); // No numbers or special chars
      request.setName("Test User");
      request.setAddress("123 Test Street");

      // When
      ResultActions result = mockMvc.perform(post("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Should fail when username already exists")
    void shouldFailWhenUsernameAlreadyExists() throws Exception {
      // Given - Create existing user
      Member existingMember = Member.builder()
          .username("existinguser")
          .password(passwordEncoder.encode("Password123@"))
          .name("Existing User")
          .address("Existing Address")
          .build();
      memberRepository.save(existingMember);

      RegisterMemberRequest request = new RegisterMemberRequest();
      request.setUsername("existinguser"); // Same username
      request.setPassword("Password123@");
      request.setName("New User");
      request.setAddress("New Address");

      // When
      ResultActions result = mockMvc.perform(post("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isConflict()) // 409 Conflict
          .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("Should fail when username contains invalid characters")
    void shouldFailWhenUsernameContainsInvalidCharacters() throws Exception {
      // Given
      RegisterMemberRequest request = new RegisterMemberRequest();
      request.setUsername("test user!"); // Contains space and !
      request.setPassword("Password123@");
      request.setName("Test User");
      request.setAddress("123 Test Street");

      // When
      ResultActions result = mockMvc.perform(post("/members/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors.username").exists());
    }
  }

  @Nested
  @DisplayName("POST /auth/login")
  class LoginTests {

    @BeforeEach
    void setUpUser() {
      // Create a test user for login tests
      Member member = Member.builder()
          .username("loginuser")
          .password(passwordEncoder.encode("Password123@"))
          .name("Login User")
          .address("Login Address")
          .build();
      memberRepository.save(member);
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void shouldLoginSuccessfully() throws Exception {
      // Given
      LoginMemberRequest request = new LoginMemberRequest();
      request.setUsername("loginuser");
      request.setPassword("Password123@");

      // When
      ResultActions result = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists())
          .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void shouldFailLoginWithWrongPassword() throws Exception {
      // Given
      LoginMemberRequest request = new LoginMemberRequest();
      request.setUsername("loginuser");
      request.setPassword("WrongPassword123@");

      // When
      ResultActions result = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail login with non-existent user")
    void shouldFailLoginWithNonExistentUser() throws Exception {
      // Given
      LoginMemberRequest request = new LoginMemberRequest();
      request.setUsername("nonexistent");
      request.setPassword("Password123@");

      // When
      ResultActions result = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail login with blank username")
    void shouldFailLoginWithBlankUsername() throws Exception {
      // Given
      LoginMemberRequest request = new LoginMemberRequest();
      request.setUsername("");
      request.setPassword("Password123@");

      // When
      ResultActions result = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail login with blank password")
    void shouldFailLoginWithBlankPassword() throws Exception {
      // Given
      LoginMemberRequest request = new LoginMemberRequest();
      request.setUsername("loginuser");
      request.setPassword("");

      // When
      ResultActions result = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)));

      // Then
      result.andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Register and Login Flow")
  class RegisterAndLoginFlowTests {

    @Test
    @DisplayName("Should be able to register and then login")
    void shouldRegisterAndLogin() throws Exception {
      // Given - Register
      RegisterMemberRequest registerRequest = new RegisterMemberRequest();
      registerRequest.setUsername("flowuser");
      registerRequest.setPassword("FlowPassword123@");
      registerRequest.setName("Flow User");
      registerRequest.setAddress("Flow Address");

      // When - Register
      mockMvc.perform(post("/members/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(registerRequest)))
          .andExpect(status().isCreated());

      // Given - Login
      LoginMemberRequest loginRequest = new LoginMemberRequest();
      loginRequest.setUsername("flowuser");
      loginRequest.setPassword("FlowPassword123@");

      // When - Login
      ResultActions loginResult = mockMvc.perform(post("/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)));

      // Then
      loginResult.andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists());
    }
  }
}

