package com.example.memberservice.dto;

import lombok.Data;

public class AuthDto {
    @Data
    public static class RegisterRequest {
        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        private String username;

        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
        @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must contain at least one digit, one lowercase, one uppercase, one special character, and no whitespace")
        private String password;

        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        private String email;
    }

    @Data
    public static class LoginRequest {
        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        private String username;
        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberValidationResponse {
        private Long userId;
        private String username;
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String username;
    }
}
