package com.blibli.apigateway;

import com.blibli.apigateway.client.MemberClient;
import com.blibli.apigateway.controller.MemberController;
import com.blibli.apigateway.dto.request.MemberDto;
import com.blibli.apigateway.dto.response.MemberErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberControllerRegisterTest {
    
    private static final String TEST_MEMBER_SERVICE_URL = "http://localhost:8007";
    private static final String TEST_REGISTER_ENDPOINT = "/api/members/register";
    
    @Mock
    private MemberClient memberClient;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private MemberController memberController;
    
    private ObjectMapper realObjectMapper;
    
    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();
        memberController = new MemberController(memberClient, realObjectMapper);
    }
    
    private Request createTestRequest() {
        return Request.create(
            Request.HttpMethod.POST,
            TEST_MEMBER_SERVICE_URL + TEST_REGISTER_ENDPOINT,
            Collections.emptyMap(),
            new byte[0],
            StandardCharsets.UTF_8,
            null
        );
    }
    
    private FeignException.BadRequest createBadRequestException(String errorJson) {
        return new FeignException.BadRequest(
            "Bad Request",
            createTestRequest(),
            errorJson.getBytes(StandardCharsets.UTF_8),
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
    
    @Test
    void testRegister_Success() {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            "Password123!",
            "1234567890"
        );
        
        MemberDto response = new MemberDto(
            "test@example.com",
            "Test Test",
            null,
            "1234567890"
        );
        
        when(memberClient.register(any(MemberDto.class))).thenReturn(response);
        ResponseEntity<?> result = memberController.register(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        
        MemberDto responseBody = (MemberDto) result.getBody();
        assertEquals("test@example.com", responseBody.getEmail());
        assertEquals("Test Test", responseBody.getFull_name());
        assertEquals("1234567890", responseBody.getPhoneNo());
        assertNull(responseBody.getPassword());
    }
    
    @Test
    void testRegister_MissingEmail() throws Exception {
        MemberDto request = new MemberDto(
            null,
            "Test Test",
            "Password123!",
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "MISSING_FIELDS",
            "Required fields missing: email"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_MissingFullName() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            null,
            "Password123!",
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "MISSING_FIELDS",
            "Required fields missing: full_name"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_MissingPassword() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            null,
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "MISSING_FIELDS",
            "Required fields missing: password"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_MissingPhoneNo() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            "Password123!",
            null
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "MISSING_FIELDS",
            "Required fields missing: phoneNo"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_InvalidEmailFormat() throws Exception {
        MemberDto request = new MemberDto(
            "invalid-email",
            "Test Test",
            "Password123!",
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "INVALID_EMAIL_FORMAT",
            "Invalid email format"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_WeakPassword_TooShort() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            "Pass1!", // Only 6 characters
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "WEAK_PASSWORD",
            "Weak password: Password must be 8-20 characters"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_WeakPassword_MissingRequirements() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            "password123", // Missing uppercase and special character
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "WEAK_PASSWORD",
            "Weak password: Password must contain atleast uppercase, lowercase, number, and special character"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_InvalidPhoneNumber() throws Exception {
        MemberDto request = new MemberDto(
            "test@example.com",
            "Test Test",
            "Password123!",
            "123456789"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "INVALID_PHONE_NUMBER",
            "Invalid phone number: Phone number must be exactly 10 digits"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException.BadRequest exception = createBadRequestException(errorJson);
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException.BadRequest thrown = assertThrows(FeignException.BadRequest.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(exception, thrown);
    }
    
    @Test
    void testRegister_EmailAlreadyExists() throws Exception {
        MemberDto request = new MemberDto(
            "existing@example.com",
            "Test Test",
            "Password123!",
            "1234567890"
        );
        
        MemberErrorResponse errorResponse = new MemberErrorResponse(
            "EMAIL_ALREADY_EXISTS",
            "Email already registered"
        );
        
        String errorJson = realObjectMapper.writeValueAsString(errorResponse);
        FeignException exception = new TestFeignException(409, "Conflict", errorJson.getBytes(StandardCharsets.UTF_8));
        
        when(memberClient.register(any(MemberDto.class))).thenThrow(exception);

        FeignException thrown = assertThrows(FeignException.class, () -> {
            memberController.register(request);
        });
        
        assertEquals(409, thrown.status());
    }
}
