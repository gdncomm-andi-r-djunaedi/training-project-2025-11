package com.gdn.project.waroenk.member.validation;

import com.gdn.project.waroenk.member.service.SystemParameterService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StrongPasswordRequiredValidator implements ConstraintValidator<StrongPasswordRequired, Object> {

  private static final String DEFAULT_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";
  private static final String DEFAULT_REQUIREMENTS = "Password must be at least 8 characters with uppercase, lowercase, number, and special character (@$!%*?&)";

  private final SystemParameterService systemParameterService;
  private String passwordField;

  @Override
  public void initialize(StrongPasswordRequired annotation) {
    this.passwordField = annotation.passwordField();
  }

  @Override
  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    if (obj == null) {
      return false;
    }

    BeanWrapper wrapper = new BeanWrapperImpl(obj);
    String password = (String) wrapper.getPropertyValue(passwordField);

    if (StringUtils.isBlank(password)) {
      addViolation(context, "Password is required");
      return false;
    }

    int minLength = Integer.parseInt(systemParameterService.getVariableData("MIN_PASSWORD_LENGTH", "8"));
    if (password.length() < minLength) {
      addViolation(context, "Password must be at least " + minLength + " characters");
      return false;
    }

    String pattern = systemParameterService.getVariableData("PASSWORD_PATTERN", DEFAULT_PATTERN);
    if (!Pattern.matches(pattern, password)) {
      String requirements = systemParameterService.getVariableData("PASSWORD_REQUIREMENTS", DEFAULT_REQUIREMENTS);
      addViolation(context, requirements);
      return false;
    }

    return true;
  }

  private void addViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}
