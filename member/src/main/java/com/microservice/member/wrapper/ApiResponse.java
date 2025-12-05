package com.microservice.member.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String errorMessage;
    private String errorCode;

    private int status;
    private String statusText;

    private Instant timestamp;

    private T data;


    public static <T> ApiResponse<T> success(T data, HttpStatus httpStatus) {
        return new ApiResponse<>(
                true,
                null,
                null,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                Instant.now(),
                data
        );
    }

    @Override
    public String toString() {
        return String.format(
                "ApiResponse [success=%s, status=%d, statusText=%s, errorCode=%s, errorMessage=%s, data=%s]",
                success, status, statusText, errorCode, errorMessage, data
        );
    }
}


