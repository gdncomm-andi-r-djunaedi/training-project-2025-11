package com.blibli.ecommerceProject.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@NoArgsConstructor
@ToString
public class ErrorResponse {

    private LocalDateTime timestamp;
    private String status;
    private String error;
    private String message;
    private Integer code;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public ErrorResponse(LocalDateTime timestamp, String status, String error, String message, Integer code) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.code = code;
    }
}

