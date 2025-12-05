package com.blibli.api_gateway.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginResponseDTO implements Serializable {
    private String token;
}
