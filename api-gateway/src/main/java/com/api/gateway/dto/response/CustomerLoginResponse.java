package com.api.gateway.dto.response;

import lombok.Data;

@Data
public class CustomerLoginResponse {
    private String customerId;
    private String email;
}
