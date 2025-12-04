package com.marketplace.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating user credentials
 * Used by API Gateway to call Member Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCredentialsRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
