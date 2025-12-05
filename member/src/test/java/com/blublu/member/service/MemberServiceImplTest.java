package com.blublu.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameExistException;
import com.blublu.member.model.request.SignUpRequest;
import com.blublu.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @InjectMocks
    MemberServiceImpl memberService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void testIsUsernameExist_True() {
        when(memberRepository.countByUsername(anyString())).thenReturn(1);

        boolean result = memberService.isUsernameExist("existingUser");

        assertTrue(result);
        verify(memberRepository).countByUsername("existingUser");
    }

    @Test
    void testIsUsernameExist_False() {
        when(memberRepository.countByUsername(anyString())).thenReturn(0);

        boolean result = memberService.isUsernameExist("newUser");

        assertFalse(result);
        verify(memberRepository).countByUsername("newUser");
    }

    @Test
    void testSignUp_Success() {
        SignUpRequest request = SignUpRequest.builder()
                .username("newUser")
                .password("password")
                .build();

        when(memberRepository.countByUsername("newUser")).thenReturn(0);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        memberService.signUp(request);

        verify(memberRepository).countByUsername("newUser");
        verify(passwordEncoder).encode("password");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void testSignUp_UsernameExists() {
        SignUpRequest request = SignUpRequest.builder()
                .username("existingUser")
                .password("password")
                .build();

        when(memberRepository.countByUsername("existingUser")).thenReturn(1);

        UsernameExistException exception = assertThrows(UsernameExistException.class, () -> {
            memberService.signUp(request);
        });

        assertEquals("Username with name existingUser exist! Please login instead", exception.getMessage());
        verify(memberRepository).countByUsername("existingUser");
    }
}
