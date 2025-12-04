package com.microservice.member;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl Unit Tests")
class MemberApplicationTests {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private MemberServiceImpl memberService;

	private RegisterRequestDto registerRequestDto;
	private LoginRequestDto loginRequestDto;
	private Member savedMember;
	private Member existingMember;

	@BeforeEach
	void setUp() {
		// Setup RegisterRequestDto
		registerRequestDto = new RegisterRequestDto();
		registerRequestDto.setEmail("test@example.com");
		registerRequestDto.setPassword("password123");
		registerRequestDto.setName("Test User");
		registerRequestDto.setPhoneNumber("1234567890");
		registerRequestDto.setAddress("123 Test Street");

		// Setup LoginRequestDto
		loginRequestDto = new LoginRequestDto();
		loginRequestDto.setEmail("test@example.com");
		loginRequestDto.setPassword("password123");

		// Setup saved Member
		savedMember = new Member();
		savedMember.setId(1L);
		savedMember.setEmail("test@example.com");
		savedMember.setName("Test User");
		savedMember.setPhoneNumber("1234567890");
		savedMember.setAddress("123 Test Street");
		savedMember.setPasswordHash("$2a$12$hashedPassword");
		savedMember.setCreatedAt(new Date());

		// Setup existing Member for login
		existingMember = new Member();
		existingMember.setId(1L);
		existingMember.setEmail("test@example.com");
		existingMember.setName("Test User");
		existingMember.setPhoneNumber("1234567890");
		existingMember.setAddress("123 Test Street");
		existingMember.setPasswordHash("$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqJYqJYqJY");
		existingMember.setCreatedAt(new Date());
	}

	@Test
	@DisplayName("Should register new user successfully")
	void testRegisterNewUser_Success() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

		// When
		RegisterResponseDto result = memberService.registerNewUser(registerRequestDto);

		// Then
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("test@example.com", result.getEmail());
		assertEquals("Test User", result.getName());
		assertEquals("Registered the user successfully", result.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should throw BusinessException when email already exists")
	void testRegisterNewUser_EmailAlreadyExists() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(true);

		// When & Then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Email address is already registered. Please use a different email or try logging in.",
				exception.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, never()).existsByPhoneNumber(anyString());
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("Should throw BusinessException when phone number already exists")
	void testRegisterNewUser_PhoneNumberAlreadyExists() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(true);

		// When & Then
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Phone number is already registered. Please use a different phone number.",
				exception.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, never()).save(any(Member.class));
	}

	@Test
	@DisplayName("Should handle DataIntegrityViolationException for duplicate email")
	void testRegisterNewUser_DataIntegrityViolationException_Email() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"Constraint violation: uc_members_email");
		when(memberRepository.save(any(Member.class))).thenThrow(exception);

		// When & Then
		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Email address is already registered. Please use a different email or try logging in.",
				businessException.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should handle DataIntegrityViolationException for duplicate phone number")
	void testRegisterNewUser_DataIntegrityViolationException_Phone() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"Constraint violation: uc_members_phone");
		when(memberRepository.save(any(Member.class))).thenThrow(exception);

		// When & Then
		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Phone number is already registered. Please use a different phone number.",
				businessException.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should handle DataIntegrityViolationException for unknown constraint")
	void testRegisterNewUser_DataIntegrityViolationException_Unknown() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"Unknown constraint violation");
		when(memberRepository.save(any(Member.class))).thenThrow(exception);

		// When & Then
		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Registration failed due to data integrity violation. Please try again.",
				businessException.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should handle unexpected exception during registration")
	void testRegisterNewUser_UnexpectedException() {
		// Given
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenThrow(new RuntimeException("Database connection error"));

		// When & Then
		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			memberService.registerNewUser(registerRequestDto);
		});

		assertEquals("Registration failed due to an unexpected error. Please try again later.",
				businessException.getMessage());

		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).existsByPhoneNumber("1234567890");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should normalize email to lowercase during registration")
	void testRegisterNewUser_EmailNormalization() {
		// Given
		registerRequestDto.setEmail("TEST@EXAMPLE.COM");
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member member = invocation.getArgument(0);
			assertEquals("test@example.com", member.getEmail());
			return savedMember;
		});

		// When
		RegisterResponseDto result = memberService.registerNewUser(registerRequestDto);

		// Then
		assertNotNull(result);
		verify(memberRepository, times(1)).existsByEmail("test@example.com");
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should trim whitespace from input fields during registration")
	void testRegisterNewUser_TrimWhitespace() {
		// Given
		registerRequestDto.setEmail("  test@example.com  ");
		registerRequestDto.setName("  Test User  ");
		registerRequestDto.setPhoneNumber("  1234567890  ");
		registerRequestDto.setAddress("  123 Test Street  ");

		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member member = invocation.getArgument(0);
			assertEquals("test@example.com", member.getEmail());
			assertEquals("Test User", member.getName());
			assertEquals("1234567890", member.getPhoneNumber());
			assertEquals("123 Test Street", member.getAddress());
			return savedMember;
		});

		// When
		RegisterResponseDto result = memberService.registerNewUser(registerRequestDto);

		// Then
		assertNotNull(result);
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Should validate user login successfully")
	void testValidateUser_Success() {
		// Given
		when(memberRepository.findByEmail("test@example.com")).thenReturn(existingMember);
		// Note: We need to use a real BCryptPasswordEncoder to test password matching
		// For this test, we'll need to encode the password properly
		String plainPassword = "password123";
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
				new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12);
		String hashedPassword = encoder.encode(plainPassword);
		existingMember.setPasswordHash(hashedPassword);
		loginRequestDto.setPassword(plainPassword);

		when(memberRepository.findByEmail("test@example.com")).thenReturn(existingMember);

		// When
		MemberLogInResponseDto result = memberService.validateUser(loginRequestDto);

		// Then
		assertNotNull(result);
		assertTrue(result.getIsMember());
		assertEquals(1L, result.getUserId());

		verify(memberRepository, times(1)).findByEmail("test@example.com");
	}

	@Test
	@DisplayName("Should throw ValidationException when user not found during login")
	void testValidateUser_UserNotFound() {
		// Given
		when(memberRepository.findByEmail(anyString())).thenReturn(null);

		// When & Then
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			memberService.validateUser(loginRequestDto);
		});

		assertEquals("User Not Found", exception.getMessage());

		verify(memberRepository, times(1)).findByEmail("test@example.com");
	}

	@Test
	@DisplayName("Should throw ValidationException when password is incorrect")
	void testValidateUser_IncorrectPassword() {
		// Given
		String plainPassword = "password123";
		String wrongPassword = "wrongpassword";
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
				new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12);
		String hashedPassword = encoder.encode(plainPassword);
		existingMember.setPasswordHash(hashedPassword);
		loginRequestDto.setPassword(wrongPassword);

		when(memberRepository.findByEmail("test@example.com")).thenReturn(existingMember);

		// When & Then
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			memberService.validateUser(loginRequestDto);
		});

		assertEquals("Incorrect Password", exception.getMessage());

		verify(memberRepository, times(1)).findByEmail("test@example.com");
	}

	@Test
	@DisplayName("Should handle case-insensitive email during login")
	void testValidateUser_CaseInsensitiveEmail() {
		// Given
		loginRequestDto.setEmail("TEST@EXAMPLE.COM");
		String plainPassword = "password123";
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
				new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12);
		String hashedPassword = encoder.encode(plainPassword);
		existingMember.setPasswordHash(hashedPassword);
		existingMember.setEmail("test@example.com");
		loginRequestDto.setPassword(plainPassword);

		when(memberRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(existingMember);

		// When
		MemberLogInResponseDto result = memberService.validateUser(loginRequestDto);

		// Then
		assertNotNull(result);
		assertTrue(result.getIsMember());
		assertEquals(1L, result.getUserId());

		verify(memberRepository, times(1)).findByEmail("TEST@EXAMPLE.COM");
	}

	@Test
	@DisplayName("Should handle null address during registration")
	void testRegisterNewUser_NullAddress() {
		// Given
		registerRequestDto.setAddress(null);
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member member = invocation.getArgument(0);
			assertNull(member.getAddress());
			return savedMember;
		});

		// When
		RegisterResponseDto result = memberService.registerNewUser(registerRequestDto);

		// Then
		assertNotNull(result);
		verify(memberRepository, times(1)).save(any(Member.class));
	}
}
