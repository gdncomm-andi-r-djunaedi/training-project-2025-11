package com.blibli.apiGateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRegisterDTO {

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Email(message = "Invalid email format")
    private String emailId;

    private String username;

    @Schema(example = "Password@123")
    @Size(min = 6, max = 20, message = "Password must be 6–20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain uppercase, lowercase, number, and special character")
    private String password;

    private String address;

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone number must be exactly 10 digits")
    private String phoneNo;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone number must be exactly 10 digits") String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(@Pattern(regexp = "^[0-9]{10}$",
            message = "Phone number must be exactly 10 digits") String phoneNo) {
        this.phoneNo = phoneNo;
    }
}
