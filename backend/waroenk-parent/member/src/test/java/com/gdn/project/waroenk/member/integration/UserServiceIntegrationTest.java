package com.gdn.project.waroenk.member.integration;

import com.gdn.project.waroenk.member.AuthenticateRequest;
import com.gdn.project.waroenk.member.UserTokenResponse;
import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.exceptions.DuplicateResourceException;
import com.gdn.project.waroenk.member.exceptions.InvalidCredentialsException;
import com.gdn.project.waroenk.member.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.member.repository.TokenRepository;
import com.gdn.project.waroenk.member.repository.UserRepository;
import com.gdn.project.waroenk.member.service.UserService;
import com.gdn.project.waroenk.member.utility.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private CacheUtil<User> userCacheUtil;

  @MockitoBean
  private CacheUtil<String> stringCacheUtil;

  private User testUser;
  private String rawPassword;

  @BeforeEach
  void setUp() {
    // Mock cache to always return null (simulate cache miss)
    when(userCacheUtil.getValue(anyString())).thenReturn(null);
    when(stringCacheUtil.getValue(anyString())).thenReturn(null);

    rawPassword = "SecureP@ss123!";

    // Create test user with encoded password
    testUser = User.builder()
        .fullName("Integration Test User")
        .email("integration.test@example.com")
        .phoneNumber("+6281111111111")
        .gender(Gender.MALE)
        .dob(LocalDate.of(1990, 1, 1))
        .passwordHash(passwordEncoder.encode(rawPassword))
        .build();
    testUser = userRepository.save(testUser);
  }

  @Nested
  @DisplayName("findUserById Integration Tests")
  class FindUserByIdIntegrationTests {

    @Test
    @DisplayName("Should find user by ID from database")
    void shouldFindUserByIdFromDatabase() {
      // When
      User result = userService.findUserById(testUser.getId().toString());

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getFullName()).isEqualTo("Integration Test User");
      assertThat(result.getEmail()).isEqualTo("integration.test@example.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent user")
    void shouldThrowExceptionForNonExistentUser() {
      // Given
      String nonExistentId = UUID.randomUUID().toString();

      // When/Then
      assertThatThrownBy(() -> userService.findUserById(nonExistentId)).isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with id: " + nonExistentId + " not found");
    }
  }


  @Nested
  @DisplayName("findUserByPhoneOrEmail Integration Tests")
  class FindUserByPhoneOrEmailIntegrationTests {

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
      // When
      User result = userService.findUserByPhoneOrEmail("integration.test@example.com");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo("integration.test@example.com");
    }

    @Test
    @DisplayName("Should find user by phone number")
    void shouldFindUserByPhoneNumber() {
      // When
      User result = userService.findUserByPhoneOrEmail("+6281111111111");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getPhoneNumber()).isEqualTo("+6281111111111");
    }
  }


  @Nested
  @DisplayName("registerUser Integration Tests")
  class RegisterUserIntegrationTests {

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
      // Given
      User newUser = User.builder()
          .fullName("New Integration User")
          .email("new.integration@example.com")
          .phoneNumber("+6282222222222")
          .gender(Gender.FEMALE)
          .dob(LocalDate.of(1995, 6, 15))
          .passwordHash(passwordEncoder.encode("NewP@ssword123!"))
          .build();

      // When
      User result = userService.registerUser(newUser);

      // Then
      assertThat(result.getId()).isNotNull();
      assertThat(result.getFullName()).isEqualTo("New Integration User");
      assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate email")
    void shouldThrowExceptionForDuplicateEmail() {
      // Given
      User duplicateUser =
          User.builder().fullName("Duplicate Email User").email("integration.test@example.com") // Same as testUser
              .phoneNumber("+6283333333333").passwordHash(passwordEncoder.encode("Password123!")).build();

      // When/Then
      assertThatThrownBy(() -> userService.registerUser(duplicateUser)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate phone")
    void shouldThrowExceptionForDuplicatePhone() {
      // Given
      User duplicateUser = User.builder()
          .fullName("Duplicate Phone User")
          .email("unique@example.com")
          .phoneNumber("+6281111111111") // Same as testUser
          .passwordHash(passwordEncoder.encode("Password123!"))
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.registerUser(duplicateUser)).isInstanceOf(DuplicateResourceException.class);
    }
  }


  @Nested
  @DisplayName("login Integration Tests")
  class LoginIntegrationTests {

    @Test
    @DisplayName("Should login successfully with email")
    void shouldLoginSuccessfullyWithEmail() {
      // Given
      AuthenticateRequest request =
          AuthenticateRequest.newBuilder().setUser("integration.test@example.com").setPassword(rawPassword).build();

      // When
      UserTokenResponse result = userService.login(request);

      // Then
      assertThat(result.getAccessToken()).isNotBlank();
      assertThat(result.getTokenType()).isEqualTo("Bearer");
      assertThat(result.getExpiresIn()).isGreaterThan(0);
      assertThat(result.getUserId()).isEqualTo(testUser.getId().toString());
    }

    @Test
    @DisplayName("Should login successfully with phone number")
    void shouldLoginSuccessfullyWithPhoneNumber() {
      // Given
      AuthenticateRequest request =
          AuthenticateRequest.newBuilder().setUser("+6281111111111").setPassword(rawPassword).build();

      // When
      UserTokenResponse result = userService.login(request);

      // Then
      assertThat(result.getAccessToken()).isNotBlank();
      assertThat(result.getUserId()).isEqualTo(testUser.getId().toString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for wrong password")
    void shouldThrowExceptionForWrongPassword() {
      // Given
      AuthenticateRequest request = AuthenticateRequest.newBuilder()
          .setUser("integration.test@example.com")
          .setPassword("WrongPassword123!")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.login(request)).isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for non-existent user")
    void shouldThrowExceptionForNonExistentUser() {
      // Given
      AuthenticateRequest request =
          AuthenticateRequest.newBuilder().setUser("nonexistent@example.com").setPassword("Password123!").build();

      // When/Then
      assertThatThrownBy(() -> userService.login(request)).isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid credentials");
    }
  }


  @Nested
  @DisplayName("updateUser Integration Tests")
  class UpdateUserIntegrationTests {

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
      // Given
      User updateRequest = User.builder()
          .id(testUser.getId())
          .fullName("Updated Name")
          .email("updated.email@example.com")
          .phoneNumber("+6284444444444")
          .gender(Gender.OTHER)
          .dob(LocalDate.of(1992, 3, 20))
          .build();

      // When
      User result = userService.updateUser(updateRequest);

      // Then
      assertThat(result.getFullName()).isEqualTo("Updated Name");
      assertThat(result.getEmail()).isEqualTo("updated.email@example.com");
      assertThat(result.getPhoneNumber()).isEqualTo("+6284444444444");
      assertThat(result.getGender()).isEqualTo(Gender.OTHER);
    }

    @Test
    @DisplayName("Should partially update user (only fullName)")
    void shouldPartiallyUpdateUser() {
      // Given
      User updateRequest = User.builder().id(testUser.getId()).fullName("Only Name Changed").build();

      // When
      User result = userService.updateUser(updateRequest);

      // Then
      assertThat(result.getFullName()).isEqualTo("Only Name Changed");
      // Other fields should remain unchanged
      assertThat(result.getEmail()).isEqualTo("integration.test@example.com");
      assertThat(result.getPhoneNumber()).isEqualTo("+6281111111111");
    }
  }
}


