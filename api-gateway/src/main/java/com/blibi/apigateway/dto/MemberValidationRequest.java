package com.blibi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for member validation
 * Used when calling Member service directly via HTTP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberValidationRequest {
    private String userName;
    private String password;
}
