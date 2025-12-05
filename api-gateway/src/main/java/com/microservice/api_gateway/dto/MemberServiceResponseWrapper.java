package com.microservice.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberServiceResponseWrapper {
    private Boolean success;
    private String errorMessage;
    private String errorCode;
    private Integer status;
    private String statusText;
    private String timestamp;
    private MemberLogInResponseDto data;
}