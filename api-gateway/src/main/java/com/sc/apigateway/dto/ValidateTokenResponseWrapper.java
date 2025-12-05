package com.sc.apigateway.dto;

import lombok.Data;

@Data
public class ValidateTokenResponseWrapper {
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private ValidateTokenResponse data;

    public static ValidateTokenResponseWrapper failure(String message) {
        ValidateTokenResponseWrapper w = new ValidateTokenResponseWrapper();
        w.setSuccess(false);
        w.setErrorMessage(message);
        return w;
    }
}
