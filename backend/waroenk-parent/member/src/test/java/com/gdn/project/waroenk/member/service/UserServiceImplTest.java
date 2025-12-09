package com.gdn.project.waroenk.member.service;

import com.gdn.project.waroenk.member.AuthenticateRequest;
import com.gdn.project.waroenk.member.ChangePasswordRequest;
import com.gdn.project.waroenk.member.ChangePasswordResponse;
import com.gdn.project.waroenk.member.FilterUserRequest;
import com.gdn.project.waroenk.member.ForgotPasswordRequest;
import com.gdn.project.waroenk.member.ForgotPasswordResponse;
import com.gdn.project.waroenk.member.LogoutRequest;
import com.gdn.project.waroenk.member.LogoutResponse;
import com.gdn.project.waroenk.member.RefreshTokenRequest;
import com.gdn.project.waroenk.member.RefreshTokenResponse;
import com.gdn.project.waroenk.member.SortBy;
import com.gdn.project.waroenk.member.UserTokenResponse;
import com.gdn.project.waroenk.member.entity.Token;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.exceptions.DuplicateResourceException;
import com.gdn.project.waroenk.member.exceptions.InvalidCredentialsException;
import com.gdn.project.waroenk.member.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.member.exceptions.ValidationException;
import com.gdn.project.waroenk.member.fixture.TestDataFactory;
import com.gdn.project.waroenk.member.repository.TokenRepository;
import com.gdn.project.waroenk.member.repository.UserRepository;
import com.gdn.project.waroenk.member.utility.CacheUtil;
import com.gdn.project.waroenk.member.utility.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private CacheUtil<User> userCacheUtil;

  @Mock
  private CacheUtil<String> stringCacheUtil;

  @Mock
  private EntityManager entityManager;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(
        userRepository,
        tokenRepository,
        passwordEncoder,
        jwtUtil,
        userCacheUtil,
        stringCacheUtil,
        entityManager
    );
    ReflectionTestUtils.setField(userService, "defaultItemPerPage", 10);
  }

  @Nested
  @DisplayName("findUserById Tests")
  class FindUserByIdTests {

    @Test
    @DisplayName("Should return cached user when available")
    void shouldReturnCachedUser() {
      // Given
      User cachedUser = TestDataFactory.createUser();
      String userId = cachedUser.getId().toString();
      String cacheKey = "user:id:" + userId;

      when(userCacheUtil.getValue(cacheKey)).thenReturn(cachedUser);

      // When
      User result = userService.findUserById(userId);

      // Then
      assertThat(result).isEqualTo(cachedUser);
      verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fetch from database and cache when not in cache")
    void shouldFetchFromDatabaseAndCache() {
      // Given
      User user = TestDataFactory.createUser();
      String userId = user.getId().toString();
      String cacheKey = "user:id:" + userId;

      when(userCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

      // When
      User result = userService.findUserById(userId);

      // Then
      assertThat(result).isEqualTo(user);
      verify(userCacheUtil).putValue(eq(cacheKey), eq(user), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      String userId = UUID.randomUUID().toString();
      String cacheKey = "user:id:" + userId;

      when(userCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(userRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> userService.findUserById(userId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with id: " + userId + " not found");
    }
  }

  @Nested
  @DisplayName("findUserByPhoneOrEmail Tests")
  class FindUserByPhoneOrEmailTests {

    @Test
    @DisplayName("Should return cached user when available")
    void shouldReturnCachedUser() {
      // Given
      User cachedUser = TestDataFactory.createUser();
      String query = cachedUser.getEmail();
      String cacheKey = "user:contact:" + query.toLowerCase();

      when(userCacheUtil.getValue(cacheKey)).thenReturn(cachedUser);

      // When
      User result = userService.findUserByPhoneOrEmail(query);

      // Then
      assertThat(result).isEqualTo(cachedUser);
      verify(userRepository, never()).findByPhoneNumberOrEmail(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when query is blank")
    void shouldThrowValidationExceptionWhenQueryBlank() {
      // When/Then
      assertThatThrownBy(() -> userService.findUserByPhoneOrEmail(""))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Phone or email query is required");
    }

    @Test
    @DisplayName("Should fetch from database and cache when not in cache")
    void shouldFetchFromDatabaseAndCache() {
      // Given
      User user = TestDataFactory.createUser();
      String query = user.getEmail();
      String normalizedQuery = query.trim().toLowerCase();
      String cacheKey = "user:contact:" + normalizedQuery;

      when(userCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(userRepository.findByPhoneNumberOrEmail(normalizedQuery, normalizedQuery))
          .thenReturn(Optional.of(user));

      // When
      User result = userService.findUserByPhoneOrEmail(query);

      // Then
      assertThat(result).isEqualTo(user);
      verify(userCacheUtil).putValue(eq(cacheKey), eq(user), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      String query = "nonexistent@email.com";
      String normalizedQuery = query.toLowerCase();
      String cacheKey = "user:contact:" + normalizedQuery;

      when(userCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(userRepository.findByPhoneNumberOrEmail(normalizedQuery, normalizedQuery))
          .thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> userService.findUserByPhoneOrEmail(query))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with phone/email: " + query + " not found");
    }
  }

  @Nested
  @DisplayName("registerUser Tests")
  class RegisterUserTests {

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
      // Given
      User newUser = TestDataFactory.createNewUser();
      User savedUser = TestDataFactory.createUser();
      savedUser.setEmail(newUser.getEmail());

      when(userRepository.save(newUser)).thenReturn(savedUser);

      // When
      User result = userService.registerUser(newUser);

      // Then
      assertThat(result).isEqualTo(savedUser);
      verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
      // Given
      User newUser = TestDataFactory.createNewUser();

      when(userRepository.save(newUser))
          .thenThrow(new DataIntegrityViolationException("email constraint"));

      // When/Then
      assertThatThrownBy(() -> userService.registerUser(newUser))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Email already registered");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone already exists")
    void shouldThrowExceptionWhenPhoneExists() {
      // Given
      User newUser = TestDataFactory.createNewUser();

      when(userRepository.save(newUser))
          .thenThrow(new DataIntegrityViolationException("phone constraint"));

      // When/Then
      assertThatThrownBy(() -> userService.registerUser(newUser))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Phone number already registered");
    }
  }

  @Nested
  @DisplayName("login Tests")
  class LoginTests {

    @Test
    @DisplayName("Should login successfully and return token")
    void shouldLoginSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      String rawPassword = TestDataFactory.generateValidPassword();
      AuthenticateRequest request = AuthenticateRequest.newBuilder()
          .setUser(user.getEmail())
          .setPassword(rawPassword)
          .build();

      when(userRepository.findByPhoneNumberOrEmail(anyString(), anyString()))
          .thenReturn(Optional.of(user));
      when(passwordEncoder.matches(rawPassword, user.getPasswordHash())).thenReturn(true);
      when(jwtUtil.generateAccessToken(user)).thenReturn("access-token");
      when(jwtUtil.generateRefreshToken()).thenReturn("refresh-token");
      when(jwtUtil.getAccessTokenExpirySeconds()).thenReturn(3600L);
      when(jwtUtil.getRefreshTokenExpiryHours()).thenReturn(24L);
      when(tokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
      when(tokenRepository.save(any(Token.class))).thenAnswer(i -> i.getArgument(0));

      // When
      UserTokenResponse result = userService.login(request);

      // Then
      assertThat(result.getAccessToken()).isEqualTo("access-token");
      assertThat(result.getTokenType()).isEqualTo("Bearer");
      assertThat(result.getExpiresIn()).isEqualTo(3600L);
      assertThat(result.getUserId()).isEqualTo(user.getId().toString());
    }

    @Test
    @DisplayName("Should throw ValidationException when credentials are blank")
    void shouldThrowValidationExceptionWhenCredentialsBlank() {
      // Given
      AuthenticateRequest request = AuthenticateRequest.newBuilder()
          .setUser("")
          .setPassword("password")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.login(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User and password are required");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      AuthenticateRequest request = AuthenticateRequest.newBuilder()
          .setUser("nonexistent@email.com")
          .setPassword("password")
          .build();

      when(userRepository.findByPhoneNumberOrEmail(anyString(), anyString()))
          .thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> userService.login(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is wrong")
    void shouldThrowExceptionWhenPasswordWrong() {
      // Given
      User user = TestDataFactory.createUser();
      AuthenticateRequest request = AuthenticateRequest.newBuilder()
          .setUser(user.getEmail())
          .setPassword("wrongpassword")
          .build();

      when(userRepository.findByPhoneNumberOrEmail(anyString(), anyString()))
          .thenReturn(Optional.of(user));
      when(passwordEncoder.matches("wrongpassword", user.getPasswordHash())).thenReturn(false);

      // When/Then
      assertThatThrownBy(() -> userService.login(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid credentials");
    }
  }

  @Nested
  @DisplayName("updateUser Tests")
  class UpdateUserTests {

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
      // Given
      User existingUser = TestDataFactory.createUser();
      User updateRequest = User.builder()
          .id(existingUser.getId())
          .fullName("Updated Name")
          .email("updated@email.com")
          .build();

      when(userCacheUtil.getValue(anyString())).thenReturn(existingUser);
      when(userRepository.save(any(User.class))).thenReturn(existingUser);

      // When
      User result = userService.updateUser(updateRequest);

      // Then
      assertThat(result.getFullName()).isEqualTo("Updated Name");
      verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email conflict")
    void shouldThrowExceptionWhenEmailConflict() {
      // Given
      User existingUser = TestDataFactory.createUser();
      User updateRequest = User.builder()
          .id(existingUser.getId())
          .fullName("Updated Name")
          .email("existing@email.com")
          .build();

      when(userCacheUtil.getValue(anyString())).thenReturn(existingUser);
      when(userRepository.save(any(User.class)))
          .thenThrow(new DataIntegrityViolationException("email constraint"));

      // When/Then
      assertThatThrownBy(() -> userService.updateUser(updateRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessage("Email already registered");
    }
  }

  @Nested
  @DisplayName("logout Tests")
  class LogoutTests {

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
      // Given
      UUID userId = UUID.randomUUID();
      LogoutRequest request = LogoutRequest.newBuilder()
          .setUserId(userId.toString())
          .setAccessToken("access-token")
          .build();

      doNothing().when(jwtUtil).blacklistToken("access-token");
      doNothing().when(tokenRepository).deleteByUserId(userId);

      // When
      LogoutResponse result = userService.logout(request);

      // Then
      assertThat(result.getSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Logout successful");
      verify(jwtUtil).blacklistToken("access-token");
      verify(tokenRepository).deleteByUserId(userId);
    }

    @Test
    @DisplayName("Should throw ValidationException when userId is blank")
    void shouldThrowValidationExceptionWhenUserIdBlank() {
      // Given
      LogoutRequest request = LogoutRequest.newBuilder()
          .setUserId("")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.logout(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID is required");
    }
  }

  @Nested
  @DisplayName("forgotPassword Tests")
  class ForgotPasswordTests {

    @Test
    @DisplayName("Should generate reset token for existing user")
    void shouldGenerateResetTokenForExistingUser() {
      // Given
      User user = TestDataFactory.createUser();
      ForgotPasswordRequest request = ForgotPasswordRequest.newBuilder()
          .setPhoneOrEmail(user.getEmail())
          .build();

      when(userRepository.findByPhoneNumberOrEmail(anyString(), anyString()))
          .thenReturn(Optional.of(user));
      when(jwtUtil.generateResetToken()).thenReturn("reset-token-123");
      when(jwtUtil.getResetTokenExpirySeconds()).thenReturn(3600L);

      // When
      ForgotPasswordResponse result = userService.forgotPassword(request);

      // Then
      assertThat(result.getSuccess()).isTrue();
      assertThat(result.getResetToken()).isEqualTo("reset-token-123");
      assertThat(result.getExpiresInSeconds()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Should return success even for non-existent user (security)")
    void shouldReturnSuccessForNonExistentUser() {
      // Given
      ForgotPasswordRequest request = ForgotPasswordRequest.newBuilder()
          .setPhoneOrEmail("nonexistent@email.com")
          .build();

      when(userRepository.findByPhoneNumberOrEmail(anyString(), anyString()))
          .thenReturn(Optional.empty());

      // When
      ForgotPasswordResponse result = userService.forgotPassword(request);

      // Then
      assertThat(result.getSuccess()).isTrue();
      assertThat(result.getResetToken()).isEmpty();
    }

    @Test
    @DisplayName("Should throw ValidationException when phoneOrEmail is blank")
    void shouldThrowValidationExceptionWhenPhoneOrEmailBlank() {
      // Given
      ForgotPasswordRequest request = ForgotPasswordRequest.newBuilder()
          .setPhoneOrEmail("")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.forgotPassword(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Phone or email is required");
    }
  }

  @Nested
  @DisplayName("changePassword Tests")
  class ChangePasswordTests {

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      String resetToken = "valid-reset-token";
      String newPassword = TestDataFactory.generateValidPassword();

      ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
          .setResetToken(resetToken)
          .setNewPassword(newPassword)
          .setConfirmPassword(newPassword)
          .build();

      when(stringCacheUtil.getValue("user:reset:" + resetToken))
          .thenReturn(user.getId().toString());
      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(passwordEncoder.encode(newPassword)).thenReturn("encoded-password");
      when(userRepository.save(user)).thenReturn(user);

      // When
      ChangePasswordResponse result = userService.changePassword(request);

      // Then
      assertThat(result.getSuccess()).isTrue();
      assertThat(result.getMessage()).isEqualTo("Password changed successfully");
      verify(stringCacheUtil).removeValue("user:reset:" + resetToken);
      verify(tokenRepository).deleteByUserId(user.getId());
    }

    @Test
    @DisplayName("Should throw ValidationException when passwords don't match")
    void shouldThrowExceptionWhenPasswordsDontMatch() {
      // Given
      ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
          .setResetToken("token")
          .setNewPassword("password1")
          .setConfirmPassword("password2")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.changePassword(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Passwords do not match");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for invalid reset token")
    void shouldThrowExceptionForInvalidResetToken() {
      // Given
      ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
          .setResetToken("invalid-token")
          .setNewPassword("password")
          .setConfirmPassword("password")
          .build();

      when(stringCacheUtil.getValue("user:reset:invalid-token")).thenReturn(null);

      // When/Then
      assertThatThrownBy(() -> userService.changePassword(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid or expired reset token");
    }
  }

  @Nested
  @DisplayName("refreshToken Tests")
  class RefreshTokenTests {

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      Token token = TestDataFactory.createToken(user);
      
      RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
          .setRefreshToken(token.getRefreshToken())
          .build();

      when(tokenRepository.findByRefreshToken(token.getRefreshToken()))
          .thenReturn(Optional.of(token));
      when(jwtUtil.generateAccessToken(user)).thenReturn("new-access-token");
      when(jwtUtil.generateRefreshToken()).thenReturn("new-refresh-token");
      when(jwtUtil.getAccessTokenExpirySeconds()).thenReturn(3600L);
      when(jwtUtil.getRefreshTokenExpiryHours()).thenReturn(24L);
      when(tokenRepository.save(token)).thenReturn(token);

      // When
      RefreshTokenResponse result = userService.refreshToken(request);

      // Then
      assertThat(result.getAccessToken()).isEqualTo("new-access-token");
      assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
      assertThat(result.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Should throw ValidationException when refresh token is blank")
    void shouldThrowValidationExceptionWhenRefreshTokenBlank() {
      // Given
      RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
          .setRefreshToken("")
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.refreshToken(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Refresh token is required");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for invalid refresh token")
    void shouldThrowExceptionForInvalidRefreshToken() {
      // Given
      RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
          .setRefreshToken("invalid-refresh-token")
          .build();

      when(tokenRepository.findByRefreshToken("invalid-refresh-token"))
          .thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> userService.refreshToken(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for expired refresh token")
    void shouldThrowExceptionForExpiredRefreshToken() {
      // Given
      User user = TestDataFactory.createUser();
      Token expiredToken = TestDataFactory.createExpiredToken(user);

      RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
          .setRefreshToken(expiredToken.getRefreshToken())
          .build();

      when(tokenRepository.findByRefreshToken(expiredToken.getRefreshToken()))
          .thenReturn(Optional.of(expiredToken));

      // When/Then
      assertThatThrownBy(() -> userService.refreshToken(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessage("Refresh token has expired");
      
      verify(tokenRepository).delete(expiredToken);
    }
  }

  @Nested
  @DisplayName("filterUsers Tests")
  class FilterUsersTests {

    @Test
    @DisplayName("Should throw ValidationException when query is too short")
    void shouldThrowValidationExceptionWhenQueryTooShort() {
      // Given
      FilterUserRequest request = FilterUserRequest.newBuilder()
          .setQuery("ab")
          .setSize(10)
          .setSortBy(SortBy.newBuilder().setField("createdAt").setDirection("DESC").build())
          .build();

      // When/Then
      assertThatThrownBy(() -> userService.filterUsers(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Filter query must be at least 3 characters");
    }
  }
}

