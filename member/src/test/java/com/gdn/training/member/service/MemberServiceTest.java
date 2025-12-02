package com.gdn.training.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gdn.training.member.dto.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gdn.training.member.dto.LoginRequest;
import com.gdn.training.member.dto.RegisterRequest;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("user@example.com");
        registerRequest.setName("Test User");
        registerRequest.setPassword("Secret123");
    }

    @Test
    void registerShouldHashPasswordAndPersistMember() {
        when(memberRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed-value");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0, Member.class));

        memberService.register(registerRequest);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member saved = memberCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(saved.getPassword()).isEqualTo("hashed-value");
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(memberRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> memberService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void validateCredentialsShouldReturnUserInfo() {
        Member member = Member.builder()
                .id(1L)
                .email(registerRequest.getEmail())
                .name(registerRequest.getName())
                .password("hashed-value")
                .role("ROLE_USER")
                .build();

        when(memberRepository.findByEmail(registerRequest.getEmail())).thenReturn(java.util.Optional.of(member));
        when(passwordEncoder.matches(eq(registerRequest.getPassword()), eq(member.getPassword()))).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(registerRequest.getEmail());
        loginRequest.setPassword(registerRequest.getPassword());

        UserInfoResponse response = memberService.validateCredentials(loginRequest);

        assertThat(response.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(response.getName()).isEqualTo(registerRequest.getName());
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }
}

