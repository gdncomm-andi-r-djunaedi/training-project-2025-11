package com.training.member.memberassignment.service.impl;

import com.training.member.memberassignment.dto.InputDTO;
import com.training.member.memberassignment.dto.OutputDTO;
import com.training.member.memberassignment.entity.MemberEntity;
import com.training.member.memberassignment.exception.MemberException;
import com.training.member.memberassignment.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private InputDTO validInput;
    private MemberEntity existingMember;

    @BeforeEach
    void setUp() {
        validInput = InputDTO.builder()
                .email("john.doe@example.com")
                .password("SecurePass123")
                .build();

        existingMember = MemberEntity.builder()
                .userId(1L)
                .email("john.doe@example.com")
                .passwordHash("$2a$10$hashedPasswordValue")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new member with valid credentials")
    void registerNewMember_Success() {
        when(memberRepository.existsByEmail(validInput.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validInput.getPassword())).thenReturn("$2a$10$hashedPasswordValue");
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(existingMember);

        assertDoesNotThrow(() -> memberService.register(validInput));

        verify(memberRepository).existsByEmail(validInput.getEmail());
        verify(passwordEncoder).encode(validInput.getPassword());
        verify(memberRepository).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when email is null during registration")
    void registerWithNullEmail_ThrowsException() {
        InputDTO inputWithNullEmail = InputDTO.builder()
                .email(null)
                .password("SecurePass123")
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(inputWithNullEmail));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email is empty string")
    void registerWithEmptyEmail_ThrowsException() {
        InputDTO inputWithEmptyEmail = InputDTO.builder()
                .email("   ")
                .password("SecurePass123")
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(inputWithEmptyEmail));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void registerWithNullPassword_ThrowsException() {
        InputDTO inputWithNullPassword = InputDTO.builder()
                .email("john.doe@example.com")
                .password(null)
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(inputWithNullPassword));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when password is empty")
    void registerWithEmptyPassword_ThrowsException() {
        InputDTO inputWithEmptyPassword = InputDTO.builder()
                .email("john.doe@example.com")
                .password("")
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(inputWithEmptyPassword));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists in database")
    void registerWithExistingEmail_ThrowsException() {
        when(memberRepository.existsByEmail(validInput.getEmail())).thenReturn(true);

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(validInput));

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("Email already registered", exception.getMessage());
        verify(memberRepository).existsByEmail(validInput.getEmail());
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should hash password before saving to database")
    void registerNewMember_PasswordIsHashed() {
        String rawPassword = "MyPassword123";
        String hashedPassword = "$2a$10$differentHashedValue";

        InputDTO input = InputDTO.builder()
                .email("test@example.com")
                .password(rawPassword)
                .build();

        when(memberRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

        memberService.register(input);

        verify(passwordEncoder).encode(rawPassword);
        verify(memberRepository).save(argThat(member -> member.getPasswordHash().equals(hashedPassword) &&
                !member.getPasswordHash().equals(rawPassword)));
    }

    @Test
    @DisplayName("Should successfully login with correct credentials")
    void loginWithValidCredentials_Success() {
        when(memberRepository.findByEmail(validInput.getEmail()))
                .thenReturn(Optional.of(existingMember));
        when(passwordEncoder.matches(validInput.getPassword(), existingMember.getPasswordHash()))
                .thenReturn(true);

        OutputDTO result = memberService.login(validInput);

        assertNotNull(result);
        assertEquals(existingMember.getEmail(), result.getEmail());
        verify(memberRepository).findByEmail(validInput.getEmail());
        verify(passwordEncoder).matches(validInput.getPassword(), existingMember.getPasswordHash());
    }

    @Test
    @DisplayName("Should throw exception when login email does not exist")
    void loginWithNonExistentEmail_ThrowsException() {
        when(memberRepository.findByEmail(validInput.getEmail())).thenReturn(Optional.empty());

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.login(validInput));

        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertEquals("Invalid email or password", exception.getMessage());
        verify(memberRepository).findByEmail(validInput.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password does not match")
    void loginWithIncorrectPassword_ThrowsException() {
        when(memberRepository.findByEmail(validInput.getEmail()))
                .thenReturn(Optional.of(existingMember));
        when(passwordEncoder.matches(validInput.getPassword(), existingMember.getPasswordHash()))
                .thenReturn(false);

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.login(validInput));

        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        verify(memberRepository).findByEmail(validInput.getEmail());
        verify(passwordEncoder).matches(validInput.getPassword(), existingMember.getPasswordHash());
    }

    @Test
    @DisplayName("Should return correct user details after successful login")
    void loginSuccess_ReturnsCorrectUserDetails() {
        MemberEntity member = MemberEntity.builder()
                .email("alice@example.com")
                .passwordHash("$2a$10$someHashValue")
                .build();

        InputDTO loginInput = InputDTO.builder()
                .email("alice@example.com")
                .password("AlicePassword123")
                .build();

        when(memberRepository.findByEmail(loginInput.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(loginInput.getPassword(), member.getPasswordHash())).thenReturn(true);

        OutputDTO result = memberService.login(loginInput);

        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should not save member when email validation fails")
    void registerWithInvalidEmail_DoesNotSaveMember() {
        InputDTO invalidInput = InputDTO.builder()
                .email("")
                .password("ValidPassword123")
                .build();

        assertThrows(MemberException.class, () -> memberService.register(invalidInput));

        verify(memberRepository, never()).existsByEmail(anyString());
        verify(memberRepository, never()).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("Should handle whitespace-only email correctly")
    void registerWithWhitespaceEmail_ThrowsException() {
        InputDTO whitespaceInput = InputDTO.builder()
                .email("     ")
                .password("ValidPassword123")
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(whitespaceInput));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should handle whitespace-only password correctly")
    void registerWithWhitespacePassword_ThrowsException() {
        InputDTO whitespaceInput = InputDTO.builder()
                .email("valid@example.com")
                .password("   ")
                .build();

        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.register(whitespaceInput));

        assertEquals("INVALID_PAYLOAD", exception.getErrorCode());
    }
}
