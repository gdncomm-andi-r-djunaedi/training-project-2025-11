package com.blibli.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
