package com.gdn.training.member.service;

import com.gdn.training.member.entity.Member;
import com.gdn.training.member.exception.UserAlreadyExistsException;
import com.gdn.training.member.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private TokenService tokenService;

    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberServiceImpl(memberRepository, passwordEncoder, tokenService);
    }

    @Test
    void registerSuccess() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword";

        when(memberRepository.existsByUsername(username)).thenReturn(false);
        when(memberRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Act
        memberService.register(username, email, password);

        // Assert
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();

        assertEquals(username, savedMember.getUsername());
        assertEquals(email, savedMember.getEmail());
        assertEquals(encodedPassword, savedMember.getHashPassword());
    }

    @Test
    void registerDuplicateUsername() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        when(memberRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> memberService.register(username, email, password));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void registerDuplicateEmail() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        when(memberRepository.existsByUsername(username)).thenReturn(false);
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> memberService.register(username, email, password));

        verify(memberRepository, never()).save(any());
    }
}
