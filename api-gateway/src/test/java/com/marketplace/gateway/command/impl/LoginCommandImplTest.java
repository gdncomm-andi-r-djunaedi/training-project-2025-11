package com.marketplace.gateway.command.impl;

import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.common.util.JwtUtil;
import com.marketplace.gateway.client.MemberServiceClient;
import com.marketplace.gateway.dto.LoginRequest;
import com.marketplace.gateway.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginCommandImplTest {

    @Mock
    private MemberServiceClient memberServiceClient;

    @Mock
    private JwtUtil jwtUtil;

    private LoginCommandImpl loginCommand;

    @BeforeEach
    void setUp() {
        loginCommand = new LoginCommandImpl(memberServiceClient, jwtUtil);
    }

    @Test
    void execute_ValidCredentials_ReturnsLoginResponse() {
        // Arrange
        String email = "user@example.com";
        String password = "Password123!";
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ROLE_USER");
        String generatedToken = "generated.jwt.token";

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        UserDetailsResponse userDetails = UserDetailsResponse.builder()
                .id(userId)
                .email(email)
                .fullName("Test User")
                .roles(roles)
                .build();

        when(memberServiceClient.validateCredentials(any(ValidateCredentialsRequest.class)))
                .thenReturn(Mono.just(userDetails));
        when(jwtUtil.generateToken(eq(userId), eq(email), eq(roles)))
                .thenReturn(generatedToken);

        // Act
        Mono<LoginResponse> result = loginCommand.execute(loginRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(generatedToken, response.getToken());
                    assertEquals("Bearer", response.getType());
                    assertEquals(userId, response.getId());
                    assertEquals(email, response.getEmail());
                })
                .verifyComplete();

        verify(memberServiceClient).validateCredentials(any(ValidateCredentialsRequest.class));
        verify(jwtUtil).generateToken(userId, email, roles);
    }

    @Test
    void execute_ValidCredentials_GeneratesJwtWithCorrectClaims() {
        // Arrange
        String email = "admin@example.com";
        String password = "AdminPass123!";
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        String generatedToken = "admin.jwt.token";

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        UserDetailsResponse userDetails = UserDetailsResponse.builder()
                .id(userId)
                .email(email)
                .fullName("Admin User")
                .roles(roles)
                .build();

        when(memberServiceClient.validateCredentials(any(ValidateCredentialsRequest.class)))
                .thenReturn(Mono.just(userDetails));
        when(jwtUtil.generateToken(eq(userId), eq(email), eq(roles)))
                .thenReturn(generatedToken);

        // Act
        Mono<LoginResponse> result = loginCommand.execute(loginRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(generatedToken, response.getToken());
                })
                .verifyComplete();

        // Verify JWT was generated with correct claims
        ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);

        verify(jwtUtil).generateToken(userIdCaptor.capture(), emailCaptor.capture(), rolesCaptor.capture());

        assertEquals(userId, userIdCaptor.getValue());
        assertEquals(email, emailCaptor.getValue());
        assertEquals(roles, rolesCaptor.getValue());
        assertTrue(rolesCaptor.getValue().contains("ROLE_ADMIN"));
    }

    @Test
    void execute_MemberServiceError_PropagatesError() {
        // Arrange
        String email = "user@example.com";
        String password = "wrongPassword";
        String errorMessage = "Invalid credentials";

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        when(memberServiceClient.validateCredentials(any(ValidateCredentialsRequest.class)))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        // Act
        Mono<LoginResponse> result = loginCommand.execute(loginRequest);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException && 
                    throwable.getMessage().equals(errorMessage))
                .verify();

        verify(memberServiceClient).validateCredentials(any(ValidateCredentialsRequest.class));
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyList());
    }

    @Test
    void execute_ValidCredentials_PassesCorrectRequestToMemberService() {
        // Arrange
        String email = "test@example.com";
        String password = "TestPass123!";
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ROLE_USER");

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        UserDetailsResponse userDetails = UserDetailsResponse.builder()
                .id(userId)
                .email(email)
                .fullName("Test User")
                .roles(roles)
                .build();

        ArgumentCaptor<ValidateCredentialsRequest> requestCaptor = 
                ArgumentCaptor.forClass(ValidateCredentialsRequest.class);

        when(memberServiceClient.validateCredentials(requestCaptor.capture()))
                .thenReturn(Mono.just(userDetails));
        when(jwtUtil.generateToken(any(), anyString(), anyList()))
                .thenReturn("token");

        // Act
        Mono<LoginResponse> result = loginCommand.execute(loginRequest);

        // Assert
        StepVerifier.create(result).expectNextCount(1).verifyComplete();

        ValidateCredentialsRequest capturedRequest = requestCaptor.getValue();
        assertEquals(email, capturedRequest.getEmail());
        assertEquals(password, capturedRequest.getPassword());
    }
}

