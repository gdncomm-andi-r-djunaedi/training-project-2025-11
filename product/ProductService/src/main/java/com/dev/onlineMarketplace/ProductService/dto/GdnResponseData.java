package com.dev.onlineMarketplace.ProductService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GdnResponseData<T> {
    private boolean success;
    private String message;
    private T data;
    private String traceId;
    
    public static <T> GdnResponseData<T> success(T data, String message) {
        return GdnResponseData.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    
    public static <T> GdnResponseData<T> success(T data) {
        return success(data, "Success");
    }
    
    public static <T> GdnResponseData<T> error(String message) {
        return GdnResponseData.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
}
