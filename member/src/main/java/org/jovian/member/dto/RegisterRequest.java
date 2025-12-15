package org.jovian.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.jovian.member.validation.ValidPassword;

public class RegisterRequest {

    @Email(message = "Invalid Email")
    @NotBlank(message = "Please enter a valid email")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}", message = "Invalid Phone Number")
    @NotBlank(message = "Please enter a valid phone number")
    private String phone;

    @ValidPassword(message = "Invalid Password")
    @NotBlank(message = "Please enter a valid password")
    private String password;

    private String username;

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getPhone(){
        return this.phone;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

}