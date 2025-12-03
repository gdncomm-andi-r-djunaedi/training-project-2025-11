package com.blibli.gdn.cartService.web.model;

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
    private T data;
    private String message;
    private String traceId;
}
