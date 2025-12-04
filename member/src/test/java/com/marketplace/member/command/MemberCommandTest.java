package com.marketplace.member.command;

import com.marketplace.common.dto.UserDetailsResponse;
import com.marketplace.common.dto.ValidateCredentialsRequest;
import com.marketplace.member.command.impl.RegisterMemberCommandImpl;
import com.marketplace.member.command.impl.ValidateCredentialsCommandImpl;
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
class MemberCommandTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterMemberCommandImpl registerMemberCommand;

    @InjectMocks
    private ValidateCredentialsCommandImpl validateCredentialsCommand;

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFullName("Test User");

        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        Member savedMember = Member.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponse response = registerMemberCommand.execute(request);

        assertNotNull(response);
        assertEquals(savedMember.getId(), response.getId());
        assertEquals(savedMember.getEmail(), response.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerMemberCommand.execute(request));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void validateCredentials_Success() {
        ValidateCredentialsRequest request = new ValidateCredentialsRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        Member member = Member.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .fullName("Test User")
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPasswordHash())).thenReturn(true);

        UserDetailsResponse response = validateCredentialsCommand.execute(request);

        assertNotNull(response);
        assertEquals(member.getId(), response.getId());
        assertEquals(member.getEmail(), response.getEmail());
    }

    @Test
    void validateCredentials_InvalidPassword_ThrowsException() {
        ValidateCredentialsRequest request = new ValidateCredentialsRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        Member member = Member.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> validateCredentialsCommand.execute(request));
    }
}
