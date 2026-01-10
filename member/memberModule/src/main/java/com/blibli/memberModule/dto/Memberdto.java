package com.blibli.memberModule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Memberdto {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Full name is required")
    private String full_name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNo;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
