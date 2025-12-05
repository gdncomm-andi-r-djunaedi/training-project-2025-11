package com.blibli.cartmodule.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@Slf4j
public class TokenUtil {

    public static String extractMemberIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing Authorization header. Please include: Authorization: Bearer <token>"
            );
        }

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid token format - token parts: {}", parts.length);
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token format"
                );
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            log.debug("Decoded token payload: {}", payload);
            String memberId = extractMemberIdFromPayload(payload);
            
            if (memberId == null || memberId.trim().isEmpty()) {
                log.warn("MemberId not found in token payload");
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "MemberId not found in token"
                );
            }

            log.debug("Extracted memberId from token: {}", memberId);
            return memberId;

        } catch (IllegalArgumentException e) {
            log.error("Error decoding token: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("Error extracting memberId from token: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Failed to extract memberId from token"
            );
        }
    }

    private static String extractMemberIdFromPayload(String payload) {
        String[] possibleKeys = {"email"};
        
        for (String key : possibleKeys) {
            int keyIndex = payload.indexOf(key);
            if (keyIndex != -1) {
                int valueStart = payload.indexOf(":", keyIndex) + 1;
                int valueEnd = payload.indexOf(",", valueStart);
                if (valueEnd == -1) {
                    valueEnd = payload.indexOf("}", valueStart);
                }
                
                if (valueEnd != -1) {
                    String value = payload.substring(valueStart, valueEnd).trim();
                    value = value.replace("\"", "").trim();
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
        }
        
        return null;
    }
}

