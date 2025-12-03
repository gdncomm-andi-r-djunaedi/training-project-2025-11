package com.gdn.marketplace.member.service;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.entity.Member;
import com.gdn.marketplace.member.repository.MemberRepository;
import com.gdn.marketplace.member.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService service;

    @Mock
    private MemberRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void saveMember() {
        AuthRequest request = new AuthRequest("user", "password", "email@test.com");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(repository.save(any(Member.class))).thenReturn(new Member());

        String response = service.saveMember(request);
        assertEquals("User added to the system", response);
    }

    @Test
    void validateUser_Success() {
        AuthRequest request = new AuthRequest("user", "password", "email@test.com");
        Member member = new Member(1L, "user", "encodedPassword", "email@test.com");
        
        when(repository.findByUsername("user")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        assertTrue(service.validateUser(request));
    }

    @Test
    void validateUser_Failure() {
        AuthRequest request = new AuthRequest("user", "wrongPassword", "email@test.com");
        Member member = new Member(1L, "user", "encodedPassword", "email@test.com");

        when(repository.findByUsername("user")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertFalse(service.validateUser(request));
    }
}
