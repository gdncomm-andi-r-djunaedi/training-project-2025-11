package com.blibli.gdn.memberService.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateMemberRequest Validation Tests")
class UpdateMemberRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid name")
    void testValidRequest() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("John Doe")
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation with null name")
    void testNullName() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name(null)
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation with empty name")
    void testEmptyName() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("")
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("Name is required") || 
            v.getMessage().contains("must not be blank")));
    }

    @Test
    @DisplayName("Should fail validation with blank name")
    void testBlankName() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("   ")
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation with name too short")
    void testNameTooShort() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("A")
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("between 2 and 255 characters")));
    }

    @Test
    @DisplayName("Should fail validation with name too long")
    void testNameTooLong() {
        // Given
        String longName = "A".repeat(256);
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name(longName)
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getMessage().contains("between 2 and 255 characters")));
    }

    @Test
    @DisplayName("Should pass validation with name at minimum length")
    void testNameAtMinimumLength() {
        // Given
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name("AB")
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with name at maximum length")
    void testNameAtMaximumLength() {
        // Given
        String maxLengthName = "A".repeat(255);
        UpdateMemberRequest request = UpdateMemberRequest.builder()
                .name(maxLengthName)
                .build();

        // When
        Set<ConstraintViolation<UpdateMemberRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }
}

