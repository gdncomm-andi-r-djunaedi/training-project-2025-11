package com.blublu.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blublu.member.entity.Member;
import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    @Mock
    MemberRepository memberRepository;

    @Test
    void testLoadUserByUsername_Success() {
        Member member = new Member();
        member.setUsername("testUser");
        member.setPassword("encodedPassword");

        when(memberRepository.findByUsername("testUser")).thenReturn(member);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");

        assertEquals("testUser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        verify(memberRepository).findByUsername("testUser");
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(memberRepository.findByUsername("unknownUser")).thenReturn(null);

        assertThrows(UsernameNotExistException.class, () -> {
            customUserDetailsService.loadUserByUsername("unknownUser");
        });

        verify(memberRepository).findByUsername("unknownUser");
    }
}
