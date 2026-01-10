package com.blibli.apigateway;

import com.blibli.apigateway.client.MemberClient;
import com.blibli.apigateway.controller.AuthController;
import com.blibli.apigateway.dto.request.LoginRequest;
import com.blibli.apigateway.dto.response.LoginResponse;
import com.blibli.apigateway.dto.response.LogoutResponse;
import com.blibli.apigateway.dto.request.MemberDto;
import com.blibli.apigateway.service.AuthService;
import com.blibli.apigateway.service.JwtService;
import com.blibli.apigateway.service.TokenBlacklistService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private static final String TEST_MEMBER_SERVICE_URL = "http://localhost:8007";
    private static final String TEST_VALIDATE_MEMBER_ENDPOINT = "/api/members/validateMember";

    @Mock
    private AuthService authService;

    @Mock
    private MemberClient memberClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthController authController;

    private static final String VALID_TOKEN = "valid-token-123";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, memberClient, jwtService, tokenBlacklistService);
    }

    private Request createTestRequest() {
        return Request.create(
            Request.HttpMethod.POST,
            TEST_MEMBER_SERVICE_URL + TEST_VALIDATE_MEMBER_ENDPOINT,
            Collections.emptyMap(),
            new byte[0],
            StandardCharsets.UTF_8,
            null
        );
    }

    private static class TestFeignException extends FeignException {
        private final int statusCode;
        
        public TestFeignException(int status, String message, byte[] content) {
            super(status, message, (Throwable) null, content, (java.util.Map<String, java.util.Collection<String>>) null);
            this.statusCode = status;
        }
        
        @Override
        public int status() {
            return statusCode;
        }
    }

    private void setupValidToken() {
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(true);
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        LoginResponse response = new LoginResponse(
            "generated-token-123",
            "Login successful",
            "test@example.com"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        
        LoginResponse responseBody = result.getBody();
        assertEquals("generated-token-123", responseBody.getToken());
        assertEquals("Login successful", responseBody.getMessage());
        assertEquals("test@example.com", responseBody.getEmail());
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        
        LoginResponse responseBody = result.getBody();
        assertNull(responseBody.getToken());
        assertEquals("Invalid credentials", responseBody.getMessage());
        assertNull(responseBody.getEmail());
    }

    @Test
    void testLogin_EmailNotFound() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "Password123!");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        
        LoginResponse responseBody = result.getBody();
        assertNull(responseBody.getToken());
        assertEquals("User not found", responseBody.getMessage());
    }

    @Test
    void testLogin_ServiceError() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Authentication failed: Service unavailable"));

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        
        LoginResponse responseBody = result.getBody();
        assertNull(responseBody.getToken());
        assertTrue(responseBody.getMessage().contains("Authentication failed"));
    }

    @Test
    void testLogin_UnexpectedError() {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<LoginResponse> result = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        
        LoginResponse responseBody = result.getBody();
        assertNull(responseBody.getToken());
        assertEquals("Unexpected error", responseBody.getMessage());
    }

    @Test
    void testGetProfile_Success() {
        MemberDto member = new MemberDto(
            "test@example.com",
            "Test User",
            null,
            "1234567890"
        );

        setupValidToken();
        when(memberClient.getMemberDetails(anyString())).thenReturn(member);

        ResponseEntity<?> result = authController.getProfile(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof MemberDto);
        
        MemberDto responseBody = (MemberDto) result.getBody();
        assertEquals("test@example.com", responseBody.getEmail());
        assertEquals("Test User", responseBody.getFull_name());
        assertEquals("1234567890", responseBody.getPhoneNo());
        assertNull(responseBody.getPassword());
    }

    @Test
    void testGetProfile_MissingAuthorizationHeader() {
        ResponseEntity<?> result = authController.getProfile(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Missing Authorization header"));
    }

    @Test
    void testGetProfile_EmptyAuthorizationHeader() {
        ResponseEntity<?> result = authController.getProfile("");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Missing Authorization header"));
    }

    @Test
    void testGetProfile_InvalidToken() {
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(false);

        ResponseEntity<?> result = authController.getProfile(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Invalid or expired token"));
    }

    @Test
    void testGetProfile_MalformedToken() {
        ResponseEntity<?> result = authController.getProfile("InvalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Invalid Authorization header format"));
    }

    @Test
    void testGetProfile_TokenWithoutBearer() {
        ResponseEntity<?> result = authController.getProfile(VALID_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Invalid Authorization header format"));
    }

    @Test
    void testGetProfile_MemberNotFound() {
        setupValidToken();
        FeignException.NotFound exception = new FeignException.NotFound(
            "Not Found",
            createTestRequest(),
            "Member not found".getBytes(StandardCharsets.UTF_8),
            null
        );

        when(memberClient.getMemberDetails(anyString())).thenThrow(exception);

        ResponseEntity<?> result = authController.getProfile(BEARER_TOKEN);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Member not found"));
    }

    @Test
    void testGetProfile_MemberServiceError() {
        setupValidToken();
        FeignException exception = new TestFeignException(
            500,
            "Internal Server Error",
            "Internal server error".getBytes(StandardCharsets.UTF_8)
        );

        when(memberClient.getMemberDetails(anyString())).thenThrow(exception);

        ResponseEntity<?> result = authController.getProfile(BEARER_TOKEN);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        assertTrue(result.getBody().toString().contains("Error retrieving profile"));
    }

    @Test
    void testGetProfile_PasswordNotIncluded() {
        MemberDto member = new MemberDto(
            "test@example.com",
            "Test User",
            null,
            "1234567890"
        );

        setupValidToken();
        when(memberClient.getMemberDetails(anyString())).thenReturn(member);

        ResponseEntity<?> result = authController.getProfile(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof MemberDto);
        
        MemberDto responseBody = (MemberDto) result.getBody();
        assertNull(responseBody.getPassword());
    }

    @Test
    void testLogout_Success() {
        setupValidToken();
        doNothing().when(tokenBlacklistService).blacklistToken(anyString());

        ResponseEntity<?> result = authController.logout(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof LogoutResponse);
        
        LogoutResponse responseBody = (LogoutResponse) result.getBody();
        assertEquals("SUCCESS", responseBody.getStatus());
        assertTrue(responseBody.getMessage().contains("Logout successful"));
        
        verify(tokenBlacklistService, times(1)).blacklistToken(VALID_TOKEN);
    }

    @Test
    void testLogout_MissingAuthorizationHeader() {
        ResponseEntity<?> result = authController.logout(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        
        String responseBody = (String) result.getBody();
        assertTrue(responseBody.contains("Missing Authorization header"));
    }

    @Test
    void testLogout_EmptyAuthorizationHeader() {
        ResponseEntity<?> result = authController.logout("");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof String);
        
        String responseBody = (String) result.getBody();
        assertTrue(responseBody.contains("Missing Authorization header"));
    }

    @Test
    void testLogout_InvalidToken() {
        when(jwtService.validateToken(VALID_TOKEN)).thenReturn(false);

        ResponseEntity<?> result = authController.logout(BEARER_TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof LogoutResponse);
        
        LogoutResponse responseBody = (LogoutResponse) result.getBody();
        assertEquals("FAILED", responseBody.getStatus());
        assertTrue(responseBody.getMessage().contains("Invalid or expired token"));
        
        verify(tokenBlacklistService, never()).blacklistToken(anyString());
    }

    @Test
    void testLogout_MalformedToken() {
        ResponseEntity<?> result = authController.logout("InvalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof LogoutResponse);
        
        LogoutResponse responseBody = (LogoutResponse) result.getBody();
        assertEquals("FAILED", responseBody.getStatus());
        assertTrue(responseBody.getMessage().contains("Invalid Authorization header format"));
        
        verify(tokenBlacklistService, never()).blacklistToken(anyString());
    }

    @Test
    void testLogout_TokenBlacklisted() {
        setupValidToken();
        doNothing().when(tokenBlacklistService).blacklistToken(anyString());

        ResponseEntity<?> result = authController.logout(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(tokenBlacklistService, times(1)).blacklistToken(VALID_TOKEN);
    }

    @Test
    void testLogout_EmailExtractionFailure() {
        setupValidToken();
        doNothing().when(tokenBlacklistService).blacklistToken(anyString());

        ResponseEntity<?> result = authController.logout(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof LogoutResponse);
        
        LogoutResponse responseBody = (LogoutResponse) result.getBody();
        assertEquals("SUCCESS", responseBody.getStatus());
        
        verify(tokenBlacklistService, times(1)).blacklistToken(VALID_TOKEN);
    }

    @Test
    void testLogout_UnexpectedError() {
        setupValidToken();
        doThrow(new RuntimeException("Unexpected error")).when(tokenBlacklistService).blacklistToken(anyString());

        ResponseEntity<?> result = authController.logout(BEARER_TOKEN);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof LogoutResponse);
        
        LogoutResponse responseBody = (LogoutResponse) result.getBody();
        assertEquals("FAILED", responseBody.getStatus());
        assertTrue(responseBody.getMessage().contains("Error during logout"));
    }
}

