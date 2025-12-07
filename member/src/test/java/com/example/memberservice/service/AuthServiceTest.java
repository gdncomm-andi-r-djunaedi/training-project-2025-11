package com.example.memberservice.service;

import com.example.memberservice.dto.AuthDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.exception.DuplicateUserException;
import com.example.memberservice.exception.InvalidPasswordException;
import com.example.memberservice.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void saveUser_shouldSaveMember() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setEmail("test@test.com");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(repository.findByUsername("test")).thenReturn(Optional.empty());
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(repository.save(any(Member.class))).thenReturn(new Member());

        String result = authService.saveUser(request);

        assertEquals("User added to the system", result);
    }

    @Test
    void saveUser_shouldThrowException_whenEmailExists() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setEmail("test@test.com");

        when(repository.findByUsername("test")).thenReturn(Optional.empty());
        when(repository.findByEmail("test@test.com")).thenReturn(Optional.of(new Member()));

        DuplicateUserException exception = assertThrows(DuplicateUserException.class,
                () -> authService.saveUser(request));
        assertEquals("Email already exists: test@test.com", exception.getMessage());
    }

    @Test
    void saveUser_shouldThrowException_whenUsernameExists() {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setEmail("test@test.com");

        when(repository.findByUsername("test")).thenReturn(Optional.of(new Member()));

        DuplicateUserException exception = assertThrows(DuplicateUserException.class,
                () -> authService.saveUser(request));
        assertEquals("Username already exists: test", exception.getMessage());
    }

    @Test
    void validateUser_shouldValidateSuccessfully() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setUsername("test");
        request.setPassword("password");

        Member member = new Member();
        member.setId(1L);
        member.setUsername("test");
        member.setPassword("encodedPassword");

        when(repository.findByUsername("test")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        AuthDto.MemberValidationResponse response = authService.validateUser(request);
        assertEquals("test", response.getUsername());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void validateUser_shouldThrowException_whenUserNotFound() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setUsername("test");
        request.setPassword("password");

        when(repository.findByUsername("test")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.validateUser(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void validateUser_shouldThrowException_whenPasswordInvalid() {
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setUsername("test");
        request.setPassword("wrongpassword");

        Member member = new Member();
        member.setUsername("test");
        member.setPassword("encodedPassword");

        when(repository.findByUsername("test")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class,
                () -> authService.validateUser(request));
        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void generateToken_shouldReturnToken() {
        String username = "test";
        Long userId = 1L;
        String expectedToken = "token";

        when(jwtService.generateToken(username, "1")).thenReturn(expectedToken);

        String result = authService.generateToken(username, userId);

        assertEquals(expectedToken, result);
    }
}
