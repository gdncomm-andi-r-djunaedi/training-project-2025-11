package com.dev.onlineMarketplace.MemberService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GDNResponseData<T> {
    private int status;
    private String message;
    private T data;

    public static <T> GDNResponseData<T> success(T data, String message) {
        return new GDNResponseData<>(200, message, data);
    }

    public static <T> GDNResponseData<T> success(T data) {
        return new GDNResponseData<>(200, "Success", data);
    }

    public static <T> GDNResponseData<T> error(int status, String message) {
        return new GDNResponseData<>(status, message, null);
    }
}
