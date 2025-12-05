package com.gdn.faurihakim.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model matching Member Service's
 * Response<VerifyCredentialsWebResponse>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCredentialsResponse {
    private String code;
    private String status;
    private MemberData data;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberData {
        private String memberId;
        private String email;
        private String fullName;
    }
}
