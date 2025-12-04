package com.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing validated user details.
 * Email is used as the user identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    private UUID id;
    private String email;
    private String fullName;
    private List<String> roles;
}
