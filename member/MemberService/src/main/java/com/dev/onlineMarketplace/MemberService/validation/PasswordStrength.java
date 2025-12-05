package com.dev.onlineMarketplace.MemberService.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordStrength {
    String message() default "Password must be at least 8 characters long and contain at least one uppercase letter and one digit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
