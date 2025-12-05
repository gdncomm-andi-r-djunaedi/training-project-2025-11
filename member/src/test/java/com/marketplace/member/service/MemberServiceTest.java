package com.marketplace.member.service;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.UnauthorizedException;
import com.marketplace.common.security.JwtTokenProvider;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.LoginResponse;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.mapper.MemberMapper;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private MemberService memberService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Member member;
    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        member = Member.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .active(true)
                .build();

        memberResponse = MemberResponse.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register member successfully")
        void shouldRegisterMemberSuccessfully() {
            when(memberRepository.existsByEmail(anyString())).thenReturn(false);
            when(memberMapper.toEntity(any(RegisterRequest.class))).thenReturn(member);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(memberRepository.save(any(Member.class))).thenReturn(member);
            when(memberMapper.toResponse(any(Member.class))).thenReturn(memberResponse);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            MemberResponse result = memberService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(memberRepository.existsByEmail(anyString())).thenReturn(true);

            assertThatThrownBy(() -> memberService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already registered");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void shouldLoginSuccessfully() {
            when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(any(UUID.class), anyString())).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(any(UUID.class), anyString())).thenReturn("refreshToken");
            when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3600000L);
            when(memberMapper.toResponse(any(Member.class))).thenReturn(memberResponse);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            LoginResponse result = memberService.login(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
            assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void shouldThrowExceptionWhenEmailNotFound() {
            when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception when password is wrong")
        void shouldThrowExceptionWhenPasswordWrong() {
            when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> memberService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid email or password");
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            String token = "Bearer validToken";
            when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
            when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3600000L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            memberService.logout(token);

            verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        }
    }
}

