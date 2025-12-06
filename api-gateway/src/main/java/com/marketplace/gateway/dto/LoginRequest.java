package com.marketplace.gateway.dto;

import com.marketplace.gateway.constant.GatewayConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO for API Gateway.
 * Uses email as the login identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = GatewayConstants.ValidationMessages.EMAIL_INVALID)
    @NotBlank(message = GatewayConstants.ValidationMessages.EMAIL_REQUIRED)
    private String email;

    @NotBlank(message = GatewayConstants.ValidationMessages.PASSWORD_REQUIRED)
    private String password;
}
