package com.example.apigateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@lombok.AllArgsConstructor
public class LoginRequest {

    private String username;

    private String password;
}
