package com.blibli.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponseDTO {
    private String userEmail;
    private String userName;
    private Date userDOB;
    private String mobileNo;
}
