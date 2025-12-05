package com.blibli.memberModule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUserResponse {
    private String status;
    private String email;
    private String message;
}

