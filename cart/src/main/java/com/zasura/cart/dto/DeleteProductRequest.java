package com.zasura.cart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteProductRequest {
  @NotBlank
  @NotNull
  private String name;
  @NotBlank
  @Email
  private String email;
  @NumberFormat
  @NotBlank
  @Pattern(regexp = "^[0-9]+$", message = "PhoneNumber must contain only digits")
  private String phoneNumber;
  private String password;
}
