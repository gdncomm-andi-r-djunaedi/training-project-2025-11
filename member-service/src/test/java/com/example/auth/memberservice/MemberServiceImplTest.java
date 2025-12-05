package com.example.auth.memberservice;

import com.example.auth.memberservice.constant.Role;
import com.example.auth.memberservice.dto.LoginRequest;
import com.example.auth.memberservice.dto.LoginResponse;
import com.example.auth.memberservice.dto.MemberRegisterRequest;
import com.example.auth.memberservice.dto.MemberRegisterResponse;
import com.example.auth.memberservice.exception.BusinessException;
import com.example.auth.memberservice.model.Member;
import com.example.auth.memberservice.repository.MemberRepository;
import com.example.auth.memberservice.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        MemberRegisterRequest request = new MemberRegisterRequest("User@Example.com", "password123");

        when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        Member savedMember = Member.builder()
                .id("123")
                .email("user@example.com")
                .passwordHash("encodedPassword")
                .role(Role.CUSTOMER.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberRegisterResponse response = memberService.register(request);

        assertEquals("123", response.id());
        assertEquals("user@example.com", response.email());  // normalized

        verify(memberRepository).existsByEmail("user@example.com");
        verify(passwordEncoder).encode("password123");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void testRegisterEmailAlreadyUsed() {
        MemberRegisterRequest request = new MemberRegisterRequest("USER@example.com", "password123");

        when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            memberService.register(request);
        });

        assertEquals("EMAIL_ALREADY_USED", ex.getCode());

        verify(memberRepository).existsByEmail("user@example.com");
        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("User@Example.com", "password123");

        Member member = Member.builder()
                .id("123")
                .email("user@example.com")
                .passwordHash("encodedPassword")
                .role(Role.CUSTOMER.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(memberRepository.save(member)).thenReturn(member);

        LoginResponse response = memberService.login(request);

        assertEquals("user@example.com", response.email());

        verify(memberRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(memberRepository).save(member);
    }

    @Test
    void testLoginInvalidEmail() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> memberService.login(request));

        assertEquals("INVALID_CREDENTIALS", ex.getCode());

        verify(memberRepository).findByEmail("user@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testLoginInvalidPassword() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        Member member = Member.builder()
                .id("123")
                .email("user@example.com")
                .passwordHash("encodedPassword")
                .role(Role.CUSTOMER.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> memberService.login(request));

        assertEquals("INVALID_CREDENTIALS", ex.getCode());

        verify(memberRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testGetAllUsers() {
        Member member1 = Member.builder()
                .id("1")
                .email("user1@example.com")
                .build();

        Member member2 = Member.builder()
                .id("2")
                .email("user2@example.com")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(member1, member2), pageable, 2);

        when(memberRepository.findAll(pageable)).thenReturn(page);

        Page<MemberRegisterResponse> result = memberService.getAllUsers(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("user1@example.com", result.getContent().get(0).email());

        verify(memberRepository).findAll(pageable);
    }
}