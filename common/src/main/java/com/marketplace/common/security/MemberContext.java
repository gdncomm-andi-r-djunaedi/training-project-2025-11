package com.marketplace.common.security;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Holds the authenticated member information for the current request.
 */
@Data
@Builder
public class MemberContext {
    private UUID memberId;
    private String email;
    private String token;
}

