package com.blibli.training.member.service.impl;

import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.dto.MemberResponse;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");
    }

    @Test
    void register_ShouldHashPasswordAndSaveMember() {
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        
        Member savedMember = Member.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .build();
                
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponse response = memberService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(passwordEncoder).encode("password");
        verify(memberRepository).save(any(Member.class));
    }
}
