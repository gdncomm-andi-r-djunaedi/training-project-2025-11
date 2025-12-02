package com.example.member.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;
    private Object paging;

    public APIResponse(int code, HttpStatus status, T data, Object metadata, String message) {
        this.code = code;
        this.status = status;
        this.data = data;
        this.paging = metadata;
        this.message = message;
    }
}
