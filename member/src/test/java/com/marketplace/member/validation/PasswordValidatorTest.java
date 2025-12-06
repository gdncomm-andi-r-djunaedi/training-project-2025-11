package com.marketplace.member.validation;

import com.marketplace.member.config.MemberConfigProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @Mock
    private MemberConfigProperties memberConfigProperties;

    @Mock
    private MemberConfigProperties.Security security;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        when(memberConfigProperties.getSecurity()).thenReturn(security);
        when(security.getPasswordMinLength()).thenReturn(8);
        
        passwordValidator = new PasswordValidator(memberConfigProperties);
    }

    private void setupMocksForInvalidPassword() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_ValidPassword_ReturnsTrue() {
        String validPassword = "Password123!";

        boolean result = passwordValidator.isValid(validPassword, context);

        assertTrue(result);
    }

    @Test
    void isValid_NullPassword_ReturnsFalse() {
        boolean result = passwordValidator.isValid(null, context);

        assertFalse(result);
    }

    @Test
    void isValid_TooShortPassword_ReturnsFalse() {
        setupMocksForInvalidPassword();
        String shortPassword = "Pass1!";  // Only 6 characters

        boolean result = passwordValidator.isValid(shortPassword, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_NoUppercase_ReturnsFalse() {
        setupMocksForInvalidPassword();
        String noUppercase = "password123!";

        boolean result = passwordValidator.isValid(noUppercase, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_NoLowercase_ReturnsFalse() {
        setupMocksForInvalidPassword();
        String noLowercase = "PASSWORD123!";

        boolean result = passwordValidator.isValid(noLowercase, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_NoDigit_ReturnsFalse() {
        setupMocksForInvalidPassword();
        String noDigit = "Password!!!";

        boolean result = passwordValidator.isValid(noDigit, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_NoSpecialCharacter_ReturnsFalse() {
        setupMocksForInvalidPassword();
        String noSpecialChar = "Password123";

        boolean result = passwordValidator.isValid(noSpecialChar, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Password1!",      // Exactly 8 characters with all requirements
            "MyP@ssw0rd",      // 10 characters
            "SuperSecure123!", // 16 characters
            "Test1234!@#$",    // Multiple special characters
            "A1b2c3d4!"        // Mixed case and numbers
    })
    void isValid_VariousValidPasswords_ReturnsTrue(String password) {
        boolean result = passwordValidator.isValid(password, context);

        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",         // Too short
            "alllowercase1!", // No uppercase
            "ALLUPPERCASE1!", // No lowercase
            "NoDigitsHere!",   // No digit
            "NoSpecial123"     // No special character
    })
    void isValid_VariousInvalidPasswords_ReturnsFalse(String password) {
        setupMocksForInvalidPassword();
        
        boolean result = passwordValidator.isValid(password, context);

        assertFalse(result);
    }

    @Test
    void isValid_AllSpecialCharacters_Recognized() {
        // Test various special characters
        String[] specialChars = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", ",", ".", "?", "\"", ":", "{", "}", "|", "<", ">"};
        
        for (String special : specialChars) {
            String password = "Password1" + special;
            boolean result = passwordValidator.isValid(password, context);
            assertTrue(result, "Password with special character '" + special + "' should be valid");
        }
    }

    @Test
    void isValid_MultipleViolations_ReportsAll() {
        setupMocksForInvalidPassword();
        String badPassword = "bad";  // Too short, no uppercase, no digit, no special char

        boolean result = passwordValidator.isValid(badPassword, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        // Verify multiple violations were added
        verify(context, atLeast(3)).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_ExactlyMinLength_WithAllRequirements_ReturnsTrue() {
        String exactLength = "Passw1!a";  // Exactly 8 characters

        boolean result = passwordValidator.isValid(exactLength, context);

        assertTrue(result);
    }

    @Test
    void isValid_CustomMinLength_RespectsSetting() {
        // Change minimum length to 10
        when(security.getPasswordMinLength()).thenReturn(10);
        setupMocksForInvalidPassword();

        String password = "Pass123!";  // 8 characters, valid but too short

        boolean result = passwordValidator.isValid(password, context);

        assertFalse(result);
    }
}

