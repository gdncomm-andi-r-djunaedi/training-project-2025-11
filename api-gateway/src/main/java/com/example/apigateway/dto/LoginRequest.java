package com.example.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user login")
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "User password", example = "mypassword123")
    private String password;
}