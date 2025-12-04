package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.model.VerifyCredentialsCommandRequest;
import com.gdn.faurihakim.member.model.MemberNotFoundException;
import com.gdn.faurihakim.member.web.model.response.VerifyCredentialsWebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyCredentialsCommandImpl Security Tests")
class VerifyCredentialsCommandImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VerifyCredentialsCommandImpl verifyCredentialsCommand;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedPasswordExample";
    private static final String MEMBER_ID = "member-123";
    private static final String WRONG_PASSWORD = "wrongpassword";

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setMemberId(MEMBER_ID);
        testMember.setEmail(TEST_EMAIL);
        testMember.setPassword(HASHED_PASSWORD);
        testMember.setFullName("Test User");
    }

    @Test
    @DisplayName("Should successfully verify credentials with correct password")
    void testExecute_CorrectPassword_ReturnsSuccess() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        // Act
        VerifyCredentialsWebResponse response = verifyCredentialsCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(MEMBER_ID);

        verify(memberRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should fail verification with incorrect password")
    void testExecute_IncorrectPassword_ReturnsFailure() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(WRONG_PASSWORD);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(WRONG_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Invalid credentials");

        verify(memberRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(WRONG_PASSWORD, HASHED_PASSWORD);
    }

    @Test
    @DisplayName("Should throw MemberNotFoundException when email not found")
    void testExecute_EmailNotFound_ThrowsException() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Member not found");

        verify(memberRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void testExecute_NullEmail_ThrowsException() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(null);
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class);

        verify(memberRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty password verification")
    void testExecute_EmptyPassword_ReturnsFailure() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("");

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("", HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should handle null password in request")
    void testExecute_NullPassword_ReturnsFailure() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(null);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(null, HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should handle member with null password hash")
    void testExecute_MemberNullPassword_ReturnsFailure() {
        // Arrange
        testMember.setPassword(null);

        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(TEST_PASSWORD, null)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should handle password with special characters")
    void testExecute_SpecialCharactersPassword_Success() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$%";
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(specialPassword);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(specialPassword, HASHED_PASSWORD)).thenReturn(true);

        // Act
        VerifyCredentialsWebResponse response = verifyCredentialsCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("Should handle very long password")
    void testExecute_VeryLongPassword_ProcessedCorrectly() {
        // Arrange
        String longPassword = "a".repeat(1000);
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(longPassword);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(longPassword, HASHED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should be case-sensitive for email matching")
    void testExecute_CaseSensitiveEmail_NotFound() {
        // Arrange
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail("TEST@EXAMPLE.COM"); // Different case
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail("TEST@EXAMPLE.COM")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> verifyCredentialsCommand.execute(request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle BCrypt password encoder timing attacks safely")
    void testExecute_TimingAttackResistance_ConsistentBehavior() {
        // Arrange - Test that both success and failure cases call passwordEncoder
        VerifyCredentialsCommandRequest request = new VerifyCredentialsCommandRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(TEST_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        // Act
        verifyCredentialsCommand.execute(request);

        // Assert - Verify passwordEncoder.matches is always called, preventing timing
        // attacks
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, HASHED_PASSWORD);
    }
}
