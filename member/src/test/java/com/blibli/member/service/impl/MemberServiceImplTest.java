package com.blibli.member.service.impl;

import com.blibli.member.dto.LoginRequest;
import com.blibli.member.dto.MemberResponse;
import com.blibli.member.dto.RegisterRequest;
import com.blibli.member.entity.Member;
import com.blibli.member.entity.Role;
import com.blibli.member.exception.BadRequestException;
import com.blibli.member.exception.ResourceNotFoundException;
import com.blibli.member.exception.UnauthorizedException;
import com.blibli.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Implementation Tests")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private Member member;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(MEMBER_ID)
                .email(EMAIL)
                .password(HASHED_PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of(Role.CUSTOMER))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();

        loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
    }

    @Test
    @DisplayName("Should register member successfully")
    void register_Success() {
        // Given
        when(memberRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = memberService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(MEMBER_ID.toString());
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(response.getLastName()).isEqualTo(LAST_NAME);
        assertThat(response.getRoles()).contains("CUSTOMER");
        verify(memberRepository).existsByEmail(EMAIL);
        verify(passwordEncoder).encode(PASSWORD);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when email already exists")
    void register_Failure_DuplicateEmail() {
        // Given
        when(memberRepository.existsByEmail(EMAIL)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> memberService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should set CUSTOMER role by default during registration")
    void register_Success_DefaultRole() {
        // Given
        when(memberRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            assertThat(saved.getRoles()).contains(Role.CUSTOMER);
            return member;
        });

        // When
        memberService.register(registerRequest);

        // Then
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Should authenticate member successfully")
    void authenticate_Success() {
        // Given
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        // When
        MemberResponse response = memberService.authenticate(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getId()).isEqualTo(MEMBER_ID.toString());
        assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(response.getLastName()).isEqualTo(LAST_NAME);
        assertThat(response.getRoles()).contains("CUSTOMER");
        verify(memberRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(PASSWORD, HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when email not found")
    void authenticate_Failure_EmailNotFound() {
        // Given
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> memberService.authenticate(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password is incorrect")
    void authenticate_Failure_WrongPassword() {
        // Given
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> memberService.authenticate(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(passwordEncoder).matches(PASSWORD, HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when account is inactive")
    void authenticate_Failure_InactiveAccount() {
        // Given
        member.setIsActive(false);
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));

        // When/Then
        assertThatThrownBy(() -> memberService.authenticate(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Account is inactive");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should get member by ID successfully")
    void getMemberById_Success() {
        // Given
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        // When
        MemberResponse response = memberService.getMemberById(MEMBER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(MEMBER_ID.toString());
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        verify(memberRepository).findById(MEMBER_ID);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when member not found by ID")
    void getMemberById_Failure_NotFound() {
        // Given
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> memberService.getMemberById(MEMBER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("Should get member by email successfully")
    void getMemberByEmail_Success() {
        // Given
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));

        // When
        MemberResponse response = memberService.getMemberByEmail(EMAIL);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        verify(memberRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when member not found by email")
    void getMemberByEmail_Failure_NotFound() {
        // Given
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> memberService.getMemberByEmail(EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("Should convert roles to string set in response")
    void toMemberResponse_Success_RolesConversion() {
        // Given
        member.setRoles(Set.of(Role.CUSTOMER, Role.ADMIN));
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        // When
        MemberResponse response = memberService.getMemberById(MEMBER_ID);

        // Then
        assertThat(response.getRoles()).hasSize(2);
        assertThat(response.getRoles()).contains("CUSTOMER", "ADMIN");
    }
}

