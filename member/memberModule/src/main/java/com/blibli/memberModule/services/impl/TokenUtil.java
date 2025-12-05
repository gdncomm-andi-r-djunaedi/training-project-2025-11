package com.blibli.memberModule.services.impl;

import java.util.Base64;

public class TokenUtil {
    public static String extractEmailFromToken(String authHeader) {
        try {
            String token = authHeader;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid token format");
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decodedBytes, "UTF-8");

            int emailStart = payload.indexOf("\"email\":\"") + 9;
            int emailEnd = payload.indexOf("\"", emailStart);

            if (emailStart > 8 && emailEnd > emailStart) {
                return payload.substring(emailStart, emailEnd);
            }

            int subStart = payload.indexOf("\"sub\":\"") + 7;
            int subEnd = payload.indexOf("\"", subStart);

            if (subStart > 6 && subEnd > subStart) {
                return payload.substring(subStart, subEnd);
            }

            throw new RuntimeException("Email not found in token payload");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email from token: " + e.getMessage());
        }
    }
}
