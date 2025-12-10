package com.marketplace.member.validation;

import com.marketplace.member.config.MemberConfigProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for password strength.
 * Validates: minimum length, uppercase, lowercase, digit, special character.
 */
@Component
@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    private final MemberConfigProperties memberConfigProperties;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        List<String> violations = new ArrayList<>();
        int minLength = memberConfigProperties.getSecurity().getPasswordMinLength();

        if (password.length() < minLength) {
            violations.add("Password must be at least " + minLength + " characters long");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("Password must contain at least one special character (!@#$%^&*(),.?\":{}|<>)");
        }

        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            for (String violation : violations) {
                context.buildConstraintViolationWithTemplate(violation).addConstraintViolation();
            }
            return false;
        }

        return true;
    }
}
