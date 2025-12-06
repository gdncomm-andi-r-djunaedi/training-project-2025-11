package com.marketplace.member.dto;

import com.marketplace.member.constant.MemberConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for member registration.
 * Email is used as the login identifier.
 */
@Data
public class RegisterRequest {
    @Email(message = MemberConstants.ValidationMessages.EMAIL_INVALID)
    @NotBlank(message = MemberConstants.ValidationMessages.EMAIL_REQUIRED)
    private String email;

    @NotBlank(message = MemberConstants.ValidationMessages.PASSWORD_REQUIRED)
    @com.marketplace.member.validation.ValidPassword
    private String password;

    private String fullName;
    private String address;
    private String phoneNumber;
}
