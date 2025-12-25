package com.blibi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for member validation
 * Received from Member service after credential validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberValidationResponse {
    private UUID userId;
    private String userName;
    private String email;
    private boolean active;
}
