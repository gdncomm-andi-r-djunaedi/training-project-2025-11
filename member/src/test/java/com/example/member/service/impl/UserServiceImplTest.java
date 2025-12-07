package com.example.member.service.impl;

import com.example.member.dto.LoginRequestDto;
import com.example.member.dto.UserRequestDto;
import com.example.member.dto.UserResponseDTO;
import com.example.member.entity.User;
import com.example.member.exceptions.InvalidCredentialsException;
import com.example.member.exceptions.UserAlreadyExistsException;
import com.example.member.repository.UserRepository;
import com.example.member.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequestDto userRequestDto;
    private LoginRequestDto loginRequestDto;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test@example.com");
        userRequestDto.setPassword("password123");
        userRequestDto.setFirstName("John");
        userRequestDto.setLastName("Doe");
        userRequestDto.setPhoneNo("1234567890");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");

        user = new User();
        user.setUserId(userId);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNo("1234567890");
    }

    @Test
    void registerUser_validRequest_returnsUserResponse() {
        
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertEquals(userRequestDto.getEmail(), result.getEmail());
        assertEquals(userRequestDto.getFirstName(), result.getFirstName());
        assertEquals(userRequestDto.getLastName(), result.getLastName());
        assertEquals(userRequestDto.getPhoneNo(), result.getPhoneNo());
        
        verify(userRepository).findByEmail(userRequestDto.getEmail());
        verify(passwordEncoder).encode(userRequestDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginUser_validCredentials_returnsJwtToken() {
        
        String expectedToken = "jwt.token.here";
        
        when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn(expectedToken);

        String result = userService.loginUser(loginRequestDto);

        assertNotNull(result);
        assertEquals(expectedToken, result);
        
        verify(userRepository).findByEmail(loginRequestDto.getEmail());
        verify(passwordEncoder).matches(loginRequestDto.getPassword(), user.getPassword());
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void getMemberProfile_validUserId_returnsUserProfile() {
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getMemberProfile(userId.toString());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
        assertEquals(user.getPhoneNo(), result.getPhoneNo());
        
        verify(userRepository).findById(userId);
    }

    @Test
    void registerUser_withSpecialCharactersInName_handlesCorrectly() {
        
        userRequestDto.setFirstName("Jean-Pierre");
        userRequestDto.setLastName("O'Connor");
        
        User userWithSpecialChars = new User();
        userWithSpecialChars.setUserId(userId);
        userWithSpecialChars.setEmail(userRequestDto.getEmail());
        userWithSpecialChars.setPassword("encodedPassword");
        userWithSpecialChars.setFirstName("Jean-Pierre");
        userWithSpecialChars.setLastName("O'Connor");
        userWithSpecialChars.setPhoneNo(userRequestDto.getPhoneNo());

        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithSpecialChars);

        UserResponseDTO result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertEquals("Jean-Pierre", result.getFirstName());
        assertEquals("O'Connor", result.getLastName());
    }

    @Test
    void registerUser_withNullPhoneNo_registersSuccessfully() {
        
        userRequestDto.setPhoneNo(null);
        
        User userWithoutPhone = new User();
        userWithoutPhone.setUserId(userId);
        userWithoutPhone.setEmail(userRequestDto.getEmail());
        userWithoutPhone.setPassword("encodedPassword");
        userWithoutPhone.setFirstName(userRequestDto.getFirstName());
        userWithoutPhone.setLastName(userRequestDto.getLastName());
        userWithoutPhone.setPhoneNo(null);

        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithoutPhone);

        UserResponseDTO result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertNull(result.getPhoneNo());
    }

    @Test
    void registerUser_existingEmail_throwsUserAlreadyExistsException() {
        
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.of(user));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(userRequestDto)
        );
        
        assertTrue(exception.getMessage().contains(userRequestDto.getEmail()));
        verify(userRepository).findByEmail(userRequestDto.getEmail());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loginUser_invalidEmail_throwsInvalidCredentialsException() {
        
        when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginRequestDto)
        );
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(loginRequestDto.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void loginUser_invalidPassword_throwsInvalidCredentialsException() {
        
        when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginRequestDto)
        );
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(loginRequestDto.getEmail());
        verify(passwordEncoder).matches(loginRequestDto.getPassword(), user.getPassword());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void getMemberProfile_invalidUserId_throwsRuntimeException() {
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getMemberProfile(userId.toString())
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getMemberProfile_invalidUuidFormat_throwsIllegalArgumentException() {
        
        String invalidUuid = "invalid-uuid-format";

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getMemberProfile(invalidUuid)
        );
        
        verify(userRepository, never()).findById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "test.user@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com"
    })
    void registerUser_variousValidEmails_registersSuccessfully(String email) {
        
        userRequestDto.setEmail(email);
        
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setEmail(email);
        newUser.setPassword("encodedPassword");
        newUser.setFirstName(userRequestDto.getFirstName());
        newUser.setLastName(userRequestDto.getLastName());
        newUser.setPhoneNo(userRequestDto.getPhoneNo());

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        UserResponseDTO result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "wrongpassword",
            "12345678",
            "password",
            ""
    })
    void loginUser_variousInvalidPasswords_throwsException(String invalidPassword) {
        
        loginRequestDto.setPassword(invalidPassword);
        
        when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(invalidPassword, user.getPassword())).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.loginUser(loginRequestDto)
        );
    }

    @Test
    void registerUser_passwordIsEncoded_neverStoresPlainText() {
        
        String plainPassword = "myPlainPassword123";
        String encodedPassword = "encodedSecurePassword";
        userRequestDto.setPassword(plainPassword);

        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify that the saved user has encoded password, not plain text
            assertEquals(encodedPassword, savedUser.getPassword());
            assertNotEquals(plainPassword, savedUser.getPassword());
            savedUser.setUserId(userId);
            return savedUser;
        });

        UserResponseDTO result = userService.registerUser(userRequestDto);

        assertNotNull(result);
        verify(passwordEncoder).encode(plainPassword);
    }

    @Test
    void loginUser_multipleFailedAttempts_consistentlyThrowsException() {
        
        when(userRepository.findByEmail(loginRequestDto.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequestDto));
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequestDto));
        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(loginRequestDto));
        
        verify(userRepository, times(3)).findByEmail(loginRequestDto.getEmail());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void getMemberProfile_doesNotReturnPassword() {
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getMemberProfile(userId.toString());

        assertNotNull(result);
        assertNotNull(result.getEmail());
        assertNotNull(result.getFirstName());
        assertNotNull(result.getLastName());
    }
}
