package com.gdn.faurihakim.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCredentialsResponse {
    private Data data;
    private boolean success;
    private String message;

    @lombok.Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String memberId;
        private String email;
        private String fullName;
    }
}
