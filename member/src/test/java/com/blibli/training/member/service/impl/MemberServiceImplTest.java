package com.blibli.training.member.service.impl;

import com.blibli.training.member.client.AuthClient;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.MemberResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthClient authClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("testUser")
                .email("test@example.com")
                .password("password")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testUser")
                .password("password")
                .build();
    }

    @Test
    void register_Success() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponse response = memberService.register(registerRequest);

        assertNotNull(response);
        assertEquals(member.getUsername(), response.getUsername());
        assertEquals(member.getEmail(), response.getEmail());

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_Fail_UsernameExists() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.register(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void register_Fail_EmailExists() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void login_Success() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authClient.generateToken(anyString())).thenReturn("token");

        MemberResponse response = memberService.login(loginRequest);

        assertNotNull(response);
        assertEquals(member.getUsername(), response.getUsername());
        assertEquals("token", response.getToken());
    }

    @Test
    void login_Fail_InvalidUsername() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void login_Fail_InvalidPassword() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void findAll_Success() {
        List<Member> members = new ArrayList<>();
        members.add(member);
        when(memberRepository.findAll()).thenReturn(members);

        List<MemberResponse> responses = memberService.findAll();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(member.getUsername(), responses.get(0).getUsername());
    }

    @Test
    void logout_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        doNothing().when(authClient).logout(anyString());

        memberService.logout("token");

        verify(redisTemplate.opsForValue()).set(eq("blacklist:token"), eq("true"), any(Duration.class));
        verify(authClient).logout("token");
    }

    @Test
    void findByUsername_Success() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.of(member));

        MemberResponse response = memberService.findByUsername("testUser");

        assertNotNull(response);
        assertEquals(member.getUsername(), response.getUsername());
    }

    @Test
    void findByUsername_Fail_NotFound() {
        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.findByUsername("testUser");
        });

        assertEquals("Member not found", exception.getMessage());
    }
}
