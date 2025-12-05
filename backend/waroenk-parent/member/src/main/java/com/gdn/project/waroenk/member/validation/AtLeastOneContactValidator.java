package com.gdn.project.waroenk.member.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

public class AtLeastOneContactValidator implements ConstraintValidator<AtLeastOneContact, Object> {

  private String emailField;
  private String phoneField;

  @Override
  public void initialize(AtLeastOneContact annotation) {
    this.emailField = annotation.emailField();
    this.phoneField = annotation.phoneField();
  }

  @Override
  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    if (obj == null) {
      return false;
    }

    BeanWrapper wrapper = new BeanWrapperImpl(obj);
    String email = (String) wrapper.getPropertyValue(emailField);
    String phone = (String) wrapper.getPropertyValue(phoneField);

    boolean hasEmail = StringUtils.hasText(email);
    boolean hasPhone = StringUtils.hasText(phone);

    return hasEmail || hasPhone;
  }
}
