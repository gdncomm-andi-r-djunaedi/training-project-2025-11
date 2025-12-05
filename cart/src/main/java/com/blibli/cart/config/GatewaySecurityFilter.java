package com.blibli.cart.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
public class GatewaySecurityFilter implements Filter {

    private static final String GATEWAY_SIGNATURE_HEADER = "X-Gateway-Signature";
    private static final String USER_ID_HEADER = "X-User-Id";

    private String gatewaySecret;

    public void setGatewaySecret(String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String userId = httpRequest.getHeader(USER_ID_HEADER);
        String gatewaySignature = httpRequest.getHeader(GATEWAY_SIGNATURE_HEADER);

        log.info("🔒 GatewaySecurityFilter executing - Path: {}, Method: {}, UserId: {}, HasSignature: {}", 
            path, httpRequest.getMethod(), userId != null ? userId : "null", gatewaySignature != null ? "yes" : "no");

        // Skip validation for health check endpoints
        if (path.contains("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // If X-User-Id header is present, MUST have gateway signature (proves request came through gateway)
        if (userId != null && !userId.trim().isEmpty()) {
            // Verify gateway signature is present
            if (gatewaySignature == null || gatewaySignature.trim().isEmpty()) {
                log.error("SECURITY: Request rejected - Missing gateway signature header. Path: {}, IP: {}, UserId: {}", 
                    path, httpRequest.getRemoteAddr(), userId);
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":\"Request must come through API Gateway\"}");
                return;
            }

            // Verify signature matches
            String expectedSignature = generateGatewaySignature(userId, path);
            if (!gatewaySignature.equals(expectedSignature)) {
                log.error("SECURITY: Request rejected - Invalid gateway signature. Path: {}, IP: {}, Expected: {}, Received: {}", 
                    path, httpRequest.getRemoteAddr(), expectedSignature, gatewaySignature);
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid gateway signature. Request must come through API Gateway\"}");
                return;
            }

            log.debug("Gateway signature verified successfully for path: {}", path);
        }

        chain.doFilter(request, response);
    }

    private String generateGatewaySignature(String userId, String path) {
        try {
            String dataToSign = userId + "|" + path + "|" + gatewaySecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating gateway signature: {}", e.getMessage());
            // Fallback: simple hash (must match gateway implementation)
            return String.valueOf((userId + path + gatewaySecret).hashCode());
        }
    }
}

