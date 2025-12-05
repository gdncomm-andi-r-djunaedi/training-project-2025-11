package com.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthInfoResponse {
    private UUID customerId;
    private String email;
    private String status;
}
