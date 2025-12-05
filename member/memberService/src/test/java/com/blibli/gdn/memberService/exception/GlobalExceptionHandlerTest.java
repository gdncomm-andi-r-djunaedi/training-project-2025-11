package com.blibli.gdn.memberService.exception;

import com.blibli.gdn.memberService.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/members/123");
    }

    @Test
    @DisplayName("Should handle MemberNotFoundException with 404 status")
    void testHandleMemberNotFound() {
        // Given
        MemberNotFoundException ex = new MemberNotFoundException("Member not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMemberNotFound(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Member not found", response.getBody().getMessage());
        assertEquals("/api/v1/members/123", response.getBody().getPath());
    }

    @Test
    @DisplayName("Should handle MemberAlreadyExistsException with 409 status")
    void testHandleMemberAlreadyExists() {
        // Given
        MemberAlreadyExistsException ex = new MemberAlreadyExistsException("Email already registered");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMemberAlreadyExists(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Email already registered", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    @DisplayName("Should handle InvalidCredentialsException with 401 status")
    void testHandleInvalidCredentials() {
        // Given
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle BadCredentialsException with 401 status")
    void testHandleBadCredentials() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle InvalidTokenException with 401 status")
    void testHandleInvalidToken() {
        // Given
        InvalidTokenException ex = new InvalidTokenException("Invalid token");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidToken(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Missing or invalid token", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException with 403 status")
    void testHandleAccessDenied() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with 400 status")
    void testHandleValidationErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("updateMemberRequest", "name", "Name is required"));
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getDetails());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void testHandleIllegalArgument() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void testHandleGenericException() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}

