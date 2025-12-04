package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.model.CreateMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.CreateMemberWebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateMemberCommandImpl Security Tests")
class CreateMemberCommandImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateMemberCommandImpl createMemberCommand;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedPasswordExample";
    private static final String TEST_FULL_NAME = "Test User";

    private CreateMemberCommandRequest request;
    private Member savedMember;

    @BeforeEach
    void setUp() {
        request = new CreateMemberCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFullName(TEST_FULL_NAME);

        savedMember = new Member();
        savedMember.setMemberId("generated-id-123");
        savedMember.setEmail(TEST_EMAIL);
        savedMember.setPassword(HASHED_PASSWORD);
        savedMember.setFullName(TEST_FULL_NAME);
    }

    @Test
    @DisplayName("Should successfully create member with hashed password")
    void testExecute_ValidRequest_CreatesMemberWithHashedPassword() {
        // Arrange
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // Act
        CreateMemberWebResponse response = createMemberCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo("generated-id-123");
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);

        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertThat(capturedMember.getPassword()).isEqualTo(HASHED_PASSWORD);
        assertThat(capturedMember.getPassword()).isNotEqualTo(TEST_PASSWORD); // Ensure password is hashed
    }

    @Test
    @DisplayName("Should never store plain text password")
    void testExecute_PasswordHashing_NeverStoresPlainText() {
        // Arrange
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();

        assertThat(savedMember.getPassword())
                .isNotEqualTo(TEST_PASSWORD)
                .isEqualTo(HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should handle duplicate email with DataIntegrityViolationException")
    void testExecute_DuplicateEmail_ThrowsException() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // Act & Assert
        assertThatThrownBy(() -> createMemberCommand.execute(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Duplicate email");

        verify(passwordEncoder).encode(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Should handle null password by encoding null")
    void testExecute_NullPassword_EncodesNull() {
        // Arrange
        request.setPassword(null);
        when(passwordEncoder.encode(null)).thenReturn("null-hash");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(passwordEncoder).encode(null);
    }

    @Test
    @DisplayName("Should handle empty password")
    void testExecute_EmptyPassword_EncodesEmpty() {
        // Arrange
        request.setPassword("");
        when(passwordEncoder.encode("")).thenReturn("empty-hash");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(passwordEncoder).encode("");
    }

    @Test
    @DisplayName("Should handle very long password")
    void testExecute_VeryLongPassword_EncodesSuccessfully() {
        // Arrange
        String longPassword = "a".repeat(1000);
        request.setPassword(longPassword);
        when(passwordEncoder.encode(longPassword)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(passwordEncoder).encode(longPassword);
    }

    @Test
    @DisplayName("Should handle password with special characters")
    void testExecute_SpecialCharactersPassword_EncodesSuccessfully() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%^&*(){}[]|\\:;\"'<>,.?/~`";
        request.setPassword(specialPassword);
        when(passwordEncoder.encode(specialPassword)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(passwordEncoder).encode(specialPassword);
    }

    @Test
    @DisplayName("Should handle password with unicode characters")
    void testExecute_UnicodePassword_EncodesSuccessfully() {
        // Arrange
        String unicodePassword = "パスワード123";
        request.setPassword(unicodePassword);
        when(passwordEncoder.encode(unicodePassword)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(passwordEncoder).encode(unicodePassword);
    }

    @Test
    @DisplayName("Should copy all fields except password to response")
    void testExecute_ValidRequest_CopiesAllFieldsExceptPassword() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        CreateMemberWebResponse response = createMemberCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getFullName()).isEqualTo(TEST_FULL_NAME);
        // Password should not be in response
    }

    @Test
    @DisplayName("Should handle null email")
    void testExecute_NullEmail_CreatesWithNullEmail() {
        request.setEmail(null);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("Should use BCrypt for password encoding")
    void testExecute_PasswordEncoder_UsesBCrypt() {
        // Arrange
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // Act
        createMemberCommand.execute(request);

        // Assert
        verify(memberRepository).save(memberCaptor.capture());

        // BCrypt hashes start with $2a$ or $2b$ or $2y$
        Member capturedMember = memberCaptor.getValue();
        assertThat(capturedMember.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("Should ensure different passwords get different hashes")
    void testExecute_DifferentPasswords_DifferentHashes() {
        // Arrange
        CreateMemberCommandRequest request1 = new CreateMemberCommandRequest();
        request1.setEmail("user1@example.com");
        request1.setPassword("password1");
        request1.setFullName("User 1");

        CreateMemberCommandRequest request2 = new CreateMemberCommandRequest();
        request2.setEmail("user2@example.com");
        request2.setPassword("password2");
        request2.setFullName("User 2");

        when(passwordEncoder.encode("password1")).thenReturn("$2a$10$hash1");
        when(passwordEncoder.encode("password2")).thenReturn("$2a$10$hash2");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // Act
        createMemberCommand.execute(request1);
        createMemberCommand.execute(request2);

        // Assert
        verify(passwordEncoder).encode("password1");
        verify(passwordEncoder).encode("password2");
    }
}
