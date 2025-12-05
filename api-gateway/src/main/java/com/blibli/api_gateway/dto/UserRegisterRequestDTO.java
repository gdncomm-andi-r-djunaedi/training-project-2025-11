package com.blibli.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDTO {
    private String userEmail;
    private String userName;
    private Date userDOB;
    private String mobileNo;
    private String password;
}
