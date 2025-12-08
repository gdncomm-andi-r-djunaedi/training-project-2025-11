package com.demo.member.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be in a valid format")
    private String userName;
    
    private String phoneNumber;
    private String address;
    
    @NotBlank(message = "Password is required")
    private String password;
}
