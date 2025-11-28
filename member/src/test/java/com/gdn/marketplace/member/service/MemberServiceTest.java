package com.gdn.marketplace.member.service;

import com.gdn.marketplace.member.dto.AuthResponse;
import com.gdn.marketplace.member.dto.LoginRequest;
import com.gdn.marketplace.member.dto.RegisterRequest;
import com.gdn.marketplace.member.entity.Member;
import com.gdn.marketplace.member.repository.MemberRepository;
import com.gdn.marketplace.member.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private MemberService memberService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Member member;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        member = new Member();
        member.setId(1L);
        member.setUsername("testuser");
        member.setPasswordHash("encodedPassword");
        member.setEmail("test@example.com");
    }

    @Test
    void register_Success() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.register(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> memberService.register(registerRequest));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void login_Success() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        AuthResponse response = memberService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token", response.getToken());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> memberService.login(loginRequest));
    }
}
