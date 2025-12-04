package com.marketplace.member.service;

import com.marketplace.common.util.JwtUtil;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.LoginResponse;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.exception.InvalidCredentialsException;
import com.marketplace.member.exception.UserAlreadyExistsException;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

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

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        when(memberRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        Member savedMember = Member.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponse response = memberService.register(request);

        assertNotNull(response);
        assertEquals(savedMember.getId(), response.getId());
        assertEquals(savedMember.getUsername(), response.getUsername());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        when(memberRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> memberService.register(request));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Member member = Member.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .passwordHash("encodedPassword")
                .email("test@example.com")
                .build();

        when(memberRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateToken(member.getUsername())).thenReturn("jwt-token");

        LoginResponse response = memberService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(member.getUsername(), response.getUsername());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        Member member = Member.builder()
                .username("testuser")
                .passwordHash("encodedPassword")
                .build();

        when(memberRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> memberService.login(request));
    }
}
