package com.marketplace.member.service;

import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.dto.AuthResponse;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.AuthenticationException;
import com.marketplace.member.exception.DuplicateResourceException;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private MemberService memberService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Use reflection to set passwordEncoder if needed
    }

    @Test
    void register_Success() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .password("Password123!")
                .fullName("Test User")
                .build();

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);

        Member savedMember = Member.builder()
                .id(1L)
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = memberService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getMember());
        assertEquals(request.getEmail(), response.getMember().getEmail());

        verify(memberRepository).existsByEmail(request.getEmail());
        verify(memberRepository).existsByUsername(request.getUsername());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .username("testuser")
                .password("Password123!")
                .fullName("Test User")
                .build();

        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> memberService.register(request));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void login_Success() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("test@example.com")
                .password("Password123!")
                .build();

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash(hashedPassword)
                .fullName("Test User")
                .build();

        when(memberRepository.findByEmail(request.getEmailOrUsername())).thenReturn(Optional.of(member));
        when(jwtService.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        AuthResponse response = memberService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertNotNull(response.getMember());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("test@example.com")
                .password("WrongPassword123!")
                .build();

        when(memberRepository.findByEmail(request.getEmailOrUsername())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AuthenticationException.class, () -> memberService.login(request));
    }

    @Test
    void logout_Success() {
        // Given
        String token = "test-token";
        doNothing().when(tokenBlacklistService).blacklistToken(anyString());

        // When
        memberService.logout(token);

        // Then
        verify(tokenBlacklistService).blacklistToken(token);
    }
}
