package com.gdn.member.controller.webmodel.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMemberRequest {
  @NotBlank(message = "Username is required")
  @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
  @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, ., _, -")
  private String username;
  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 50, message = "Password must be at least 8 characters")
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!*_.~-]).*$",
    message = "Password must contain letters, numbers, and special characters (@#$%^&+=!*_.~-)")
  private String password;
  @NotBlank(message = "Name is required")
  private String name;
  @NotBlank(message = "Address is required")
  @Size(max = 200, message = "Address cannot exceed 200 characters")
  private String address;

}
