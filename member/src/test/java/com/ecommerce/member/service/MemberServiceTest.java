package com.ecommerce.member.service;

import com.ecommerce.member.dto.LoginDto;
import com.ecommerce.member.dto.MemberDto;
import com.ecommerce.member.entity.Member;
import com.ecommerce.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldSaveMember_WhenUsernameDoesNotExist() {
        MemberDto dto = new MemberDto();
        dto.setUsername("testuser");
        dto.setPassword("password");

        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        memberService.register(dto);

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        MemberDto dto = new MemberDto();
        dto.setUsername("testuser");

        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(new Member()));

        assertThrows(RuntimeException.class, () -> memberService.register(dto));
    }

    @Test
    void login_ShouldReturnMember_WhenCredentialsAreValid() {
        LoginDto dto = new LoginDto();
        dto.setUsername("testuser");
        dto.setPassword("password");

        Member member = new Member();
        member.setUsername("testuser");
        member.setPassword("encodedPassword");

        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        Member result = memberService.login(dto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
