package com.elfrida.member.service;

import com.elfrida.member.configuration.JwtUtil;
import com.elfrida.member.dto.LoginRequest;
import com.elfrida.member.dto.LoginResponse;
import com.elfrida.member.dto.MemberRequest;
import com.elfrida.member.exception.EmailAlreadyRegisteredException;
import com.elfrida.member.exception.InvalidCredentialsException;
import com.elfrida.member.model.Member;
import com.elfrida.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void register_shouldSaveNewMember_whenEmailNotRegistered() {
        MemberRequest request = new MemberRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("secret");

        when(memberRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        Member saved = new Member();
        saved.setId("123");
        saved.setName("John Doe");
        saved.setEmail("john@example.com");
        saved.setPassword("encoded-secret");
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        Member result = memberService.register(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member toSave = memberCaptor.getValue();

        assertThat(toSave.getName()).isEqualTo("John Doe");
        assertThat(toSave.getEmail()).isEqualTo("john@example.com");
        assertThat(toSave.getPassword()).isEqualTo("encoded-secret");

        assertThat(result.getId()).isEqualTo("123");
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyRegistered() {
        MemberRequest request = new MemberRequest();
        request.setEmail("john@example.com");

        when(memberRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(new Member()));

        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("secret");

        Member member = new Member();
        member.setName("John");
        member.setEmail("john@example.com");
        member.setPassword("encoded");

        when(memberRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        LoginResponse response = memberService.login(request);

        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldThrowException_whenEmailNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("secret");

        when(memberRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        Member member = new Member();
        member.setEmail("john@example.com");
        member.setPassword("encoded");

        when(memberRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> memberService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void logout_shouldNotFail_whenTokenIsNullOrAny() {
        memberService.logout(null);
        memberService.logout("some-token");
    }
}


