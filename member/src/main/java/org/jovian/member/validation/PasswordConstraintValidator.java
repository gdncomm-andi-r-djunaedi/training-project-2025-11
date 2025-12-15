package org.jovian.member.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.jovian.member.validation.ValidPassword;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null &&
                password.length() >= 8 &&
                password.length() <= 20 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[A-Z].*");
    }
}
