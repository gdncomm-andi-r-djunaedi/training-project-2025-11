package com.gdn.project.waroenk.member.dto;

import com.gdn.project.waroenk.member.validation.AtLeastOneContact;
import com.gdn.project.waroenk.member.validation.StrongPasswordRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@AtLeastOneContact()
@StrongPasswordRequired()
public record CreateUserRequestDto(

    @NotBlank(message = "Full name is required") @Size(max = 255) String fullName,

    @Email(message = "Invalid email format") @Size(max = 255) String email,

    @Pattern(regexp = "^\\+?[\\d+]{10,15}$", message = "Invalid phone number format") @Size(max = 50) String phone,

    String password,

    LocalDate dob,

    @Pattern(regexp = "^[MFO]$", message = "Gender must be M, F, or O") String gender) {
}
