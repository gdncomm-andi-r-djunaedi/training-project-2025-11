package com.microservice.member;

import com.microservice.member.dto.LoginRequestDto;
import com.microservice.member.dto.MemberLogInResponseDto;
import com.microservice.member.dto.RegisterRequestDto;
import com.microservice.member.dto.RegisterResponseDto;
import com.microservice.member.entity.Member;
import com.microservice.member.exceptions.BusinessException;
import com.microservice.member.exceptions.ValidationException;
import com.microservice.member.repository.MemberRepository;
import com.microservice.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    // Mock the repository (fake database)
    @Mock
    private MemberRepository memberRepository;

    // The service we want to test
    @InjectMocks
    private MemberServiceImpl memberService;

    // Test data
    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        // Setup test data before each test

        // Create a registration request
        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("John Doe");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setAddress("123 Main St");

        // Create a login request
        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Create a saved member (as if saved in database)
        savedMember = new Member();
        savedMember.setId(1L);
        savedMember.setEmail("test@example.com");
        savedMember.setName("John Doe");
        savedMember.setPhoneNumber("1234567890");
        savedMember.setAddress("123 Main St");
        savedMember.setCreatedAt(new Date());
    }

    // ========== REGISTRATION TESTS ==========

    @Test
    void testRegisterUser_Success() {
        // Given: Email and phone don't exist in database
        when(memberRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(memberRepository.existsByPhoneNumber("1234567890")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When: We try to register
        RegisterResponseDto result = memberService.registerNewUser(registerRequest);

        // Then: Registration should succeed
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Given: Email already exists in database
        when(memberRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then: Should throw exception
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.registerNewUser(registerRequest);
        });

        assertEquals("Email address is already registered. Please use a different email or try logging in.",
                exception.getMessage());
    }

    @Test
    void testRegisterUser_PhoneAlreadyExists() {
        // Given: Phone number already exists
        when(memberRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(memberRepository.existsByPhoneNumber("1234567890")).thenReturn(true);

        // When & Then: Should throw exception
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            memberService.registerNewUser(registerRequest);
        });

        assertEquals("Phone number is already registered. Please use a different phone number.",
                exception.getMessage());
    }

    // ========== LOGIN TESTS ==========

    @Test
    void testLogin_Success() {
        // Given: User exists in database with correct password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hashedPassword = encoder.encode("password123");
        savedMember.setPasswordHash(hashedPassword);

        when(memberRepository.findByEmail("test@example.com")).thenReturn(savedMember);

        // When: We try to login
        MemberLogInResponseDto result = memberService.validateUser(loginRequest);

        // Then: Login should succeed
        assertNotNull(result);
        assertTrue(result.getIsMember());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void testLogin_UserNotFound() {
        // Given: User doesn't exist in database
        when(memberRepository.findByEmail("test@example.com")).thenReturn(null);

        // When & Then: Should throw exception
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            memberService.validateUser(loginRequest);
        });

        assertEquals("User Not Found", exception.getMessage());
    }

    @Test
    void testLogin_WrongPassword() {
        // Given: User exists but password is wrong
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String correctPasswordHash = encoder.encode("password123");
        savedMember.setPasswordHash(correctPasswordHash);

        loginRequest.setPassword("wrongpassword"); // Wrong password

        when(memberRepository.findByEmail("test@example.com")).thenReturn(savedMember);

        // When & Then: Should throw exception
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            memberService.validateUser(loginRequest);
        });

        assertEquals("Incorrect Password", exception.getMessage());
    }
}