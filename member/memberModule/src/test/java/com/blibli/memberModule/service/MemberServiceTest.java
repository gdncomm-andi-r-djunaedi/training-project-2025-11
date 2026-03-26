package com.blibli.memberModule.service;

import com.blibli.memberModule.dto.LoginRequestDto;
import com.blibli.memberModule.dto.LoginResponseDto;
import com.blibli.memberModule.dto.MemberRequestDto;
import com.blibli.memberModule.dto.MemberResponseDto;
import com.blibli.memberModule.entity.Member;
import com.blibli.memberModule.repository.MemberRepository;
import com.blibli.memberModule.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private MemberServiceImpl memberService;

  private MemberRequestDto memberRequestDto;
  private LoginRequestDto loginRequestDto;
  private Member member;
  private String encodedPassword;

  @BeforeEach
  void setUp() {
    memberRequestDto = new MemberRequestDto();
    memberRequestDto.setEmail("test@gmail.com");
    memberRequestDto.setPassword("password123");
    memberRequestDto.setName("Test User");
    memberRequestDto.setPhone("1234567890");

    loginRequestDto = new LoginRequestDto();
    loginRequestDto.setEmail("test@gmail.com");
    loginRequestDto.setPassword("password123");

    encodedPassword = "$2a$10$encodedPasswordHash";

    member = new Member();
    member.setMemberId(1L);
    member.setEmail("test@gmail.com");
    member.setPassword(encodedPassword);
    member.setName("Test User");
    member.setPhone("1234567890");
    member.setCreatedAt(LocalDateTime.now());
    member.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  void testRegister_Success() {
    when(memberRepository.existsByEmail("test@gmail.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
    when(memberRepository.save(any(Member.class))).thenReturn(member);

    MemberResponseDto result = memberService.register(memberRequestDto);

    assertNotNull(result);
    assertEquals(1L, result.getMemberId());
    assertEquals("test@gmail.com", result.getEmail());
    assertEquals("Test User", result.getName());
    verify(memberRepository, times(1)).existsByEmail("test@gmail.com");
    verify(passwordEncoder, times(1)).encode("password123");
    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  void testRegister_EmailAlreadyExists() {
    when(memberRepository.existsByEmail("test@gmail.com")).thenReturn(true);

    assertThrows(RuntimeException.class, () -> {
      memberService.register(memberRequestDto);
    });

    verify(memberRepository, times(1)).existsByEmail("test@gmail.com");
    verify(memberRepository, never()).save(any(Member.class));
  }

  @Test
  void testLogin_Success() {
    when(memberRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("password123", encodedPassword)).thenReturn(true);

    LoginResponseDto result = memberService.login(loginRequestDto);

    assertNotNull(result);
    assertNotNull(result.getMember());
    assertEquals(1L, result.getMember().getMemberId());
    assertEquals("test@gmail.com", result.getMember().getEmail());
    verify(memberRepository, times(1)).findByEmail("test@gmail.com");
    verify(passwordEncoder, times(1)).matches("password123", encodedPassword);
  }

  @Test
  void testLogin_EmailNotFound() {
    when(memberRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> {
      memberService.login(loginRequestDto);
    });

    verify(memberRepository, times(1)).findByEmail("test@gmail.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  void testLogin_InvalidPassword() {
    when(memberRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("password123", encodedPassword)).thenReturn(false);

    assertThrows(RuntimeException.class, () -> {
      memberService.login(loginRequestDto);
    });

    verify(memberRepository, times(1)).findByEmail("test@gmail.com");
    verify(passwordEncoder, times(1)).matches("password123", encodedPassword);
  }

  @Test
  void testLogout_Success() {
    when(memberRepository.findById("1")).thenReturn(Optional.of(member));
    memberService.logout(1L);
    verify(memberRepository, times(1)).findById("1");
  }

  @Test
  void testLogout_MemberNotFound() {
    when(memberRepository.findById("1")).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> {
      memberService.logout(1L);
    });
    verify(memberRepository, times(1)).findById("1");
  }
}

