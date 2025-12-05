package com.gdn.faurihakim.gateway.service;

import com.gdn.faurihakim.gateway.model.AuthRequest;
import com.gdn.faurihakim.gateway.model.VerifyCredentialsResponse;
import com.gdn.faurihakim.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Security Tests")
class AuthServiceTest {

        @Mock
        private WebClient.Builder webClientBuilder;

        @Mock
        private WebClient webClient;

        @Mock
        private WebClient.RequestBodyUriSpec requestBodyUriSpec;

        @Mock
        private WebClient.RequestBodySpec requestBodySpec;

        @Mock
        private WebClient.RequestHeadersSpec requestHeadersSpec;

        @Mock
        private WebClient.ResponseSpec responseSpec;

        @Mock
        private JwtUtil jwtUtil;

        @InjectMocks
        private AuthService authService;

        private static final String TEST_EMAIL = "test@example.com";
        private static final String TEST_PASSWORD = "password123";
        private static final String TEST_USER_ID = "user-123";
        private static final String TEST_JWT_TOKEN = "generated.jwt.token";
        private static final String MEMBER_SERVICE_URL = "http://localhost:8081";

        @BeforeEach
        void setUp() {
                // Set member service URL via reflection
                ReflectionTestUtils.setField(authService, "memberServiceUrl", MEMBER_SERVICE_URL);
                when(webClientBuilder.build()).thenReturn(webClient);
                when(webClient.post()).thenReturn(requestBodyUriSpec);
                when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
                when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
                when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void testLogin_ValidCredentials_ReturnsJwtToken() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                VerifyCredentialsResponse.Data data = new VerifyCredentialsResponse.Data();
                data.setMemberId(TEST_USER_ID);

                VerifyCredentialsResponse verifyResponse = new VerifyCredentialsResponse();
                verifyResponse.setSuccess(true);
                verifyResponse.setData(data);

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.just(verifyResponse));
                when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_JWT_TOKEN);

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .expectNext(TEST_JWT_TOKEN)
                                .verifyComplete();

                verify(jwtUtil).generateToken(TEST_USER_ID);
                verify(requestBodySpec).bodyValue(request);
        }

        @Test
        @DisplayName("Should return empty when credentials are invalid")
        void testLogin_InvalidCredentials_ReturnsEmpty() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail("test@mail.com");
                request.setPassword("wrong-password");

                VerifyCredentialsResponse verifyResponse = VerifyCredentialsResponse.builder()
                                .success(false)
                                .message("Invalid")
                                .data(null)
                                .build();

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.just(verifyResponse));

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();

                verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle member service 401 error gracefully")
        void testLogin_MemberService401_ReturnsEmpty() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                WebClientResponseException unauthorizedException = WebClientResponseException.Unauthorized.create(
                                401, "Unauthorized", null, null, null);

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.error(unauthorizedException));

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();

                verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle member service connection error")
        void testLogin_MemberServiceDown_ReturnsEmpty() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();

                verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle null response data")
        void testLogin_NullResponseData_ReturnsEmpty() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                VerifyCredentialsResponse verifyResponse = new VerifyCredentialsResponse();
                verifyResponse.setSuccess(true);
                verifyResponse.setData(null); // Null data

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.just(verifyResponse));

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();

                verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle empty response from member service")
        void testLogin_EmptyResponse_ReturnsEmpty() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.empty());

                // Act
                Mono<String> result = authService.login(request);

                // Assert
                StepVerifier.create(result)
                                .verifyComplete();

                verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should call correct member service URL")
        void testLogin_CallsCorrectUrl() {
                // Arrange
                AuthRequest request = new AuthRequest();
                request.setEmail(TEST_EMAIL);
                request.setPassword(TEST_PASSWORD);

                VerifyCredentialsResponse.Data data = new VerifyCredentialsResponse.Data();
                data.setMemberId(TEST_USER_ID);

                VerifyCredentialsResponse verifyResponse = new VerifyCredentialsResponse();
                verifyResponse.setSuccess(true);
                verifyResponse.setData(data);

                when(responseSpec.bodyToMono(VerifyCredentialsResponse.class))
                                .thenReturn(Mono.just(verifyResponse));
                when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_JWT_TOKEN);

                // Act
                authService.login(request).subscribe();

                // Assert
                verify(requestBodyUriSpec).uri(MEMBER_SERVICE_URL + "/api/members/verify");
        }
}
