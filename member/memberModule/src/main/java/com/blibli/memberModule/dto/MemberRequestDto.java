package com.blibli.memberModule.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {

  @Email(message = "Email must be valid")
  private String email;
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;
  private String name;
  private String phone;
}

