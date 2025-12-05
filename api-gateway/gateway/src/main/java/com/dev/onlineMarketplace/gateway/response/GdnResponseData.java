package com.dev.onlineMarketplace.gateway.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GdnResponseData<T> {
    private boolean success;
    private String message;
    private T data;
    private String traceId;
    private int status;

    public static <T> GdnResponseData<T> success(T data, String message) {
        return GdnResponseData.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .build();
    }

    public static <T> GdnResponseData<T> error(int status, String message) {
        return GdnResponseData.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
}
