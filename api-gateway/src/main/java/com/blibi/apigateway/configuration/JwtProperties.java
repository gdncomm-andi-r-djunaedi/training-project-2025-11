package com.blibi.apigateway.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT token generation and validation.
 * Supports both symmetric (HS256) and asymmetric (RS256) key algorithms.
 * 
 * Topics to be learned: JWT, OAuth2, Symmetric vs Asymmetric Cryptography
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT signing algorithm: HS256 (symmetric) or RS256 (asymmetric)
     */
    private String algorithm = "HS256";

    /**
     * Secret key for symmetric algorithm (HS256)
     * Should be at least 256 bits (32 characters) for HS256
     */
    private String secretKey;

    /**
     * RSA public key for asymmetric algorithm (RS256)
     * Used for token validation
     */
    private String rsaPublicKey;

    /**
     * RSA private key for asymmetric algorithm (RS256)
     * Used for token signing
     */
    private String rsaPrivateKey;

    /**
     * Token expiration time in milliseconds
     * Default: 24 hours (86400000 ms)
     */
    private long expirationMs = 86400000L;

    /**
     * JWT issuer claim (iss)
     * Identifies the principal that issued the JWT
     */
    private String issuer = "api-gateway";

    /**
     * JWT audience claim (aud)
     * Identifies the recipients that the JWT is intended for
     */
    private String audience = "online-marketplace";

    /**
     * Cookie name for JWT token
     */
    private String cookieName = "JWT_TOKEN";

    /**
     * Cookie domain
     */
    private String cookieDomain = "localhost";

    /**
     * Cookie path
     */
    private String cookiePath = "/";

    /**
     * Cookie secure flag (HTTPS only)
     */
    private boolean cookieSecure = false;

    /**
     * Cookie HttpOnly flag
     */
    private boolean cookieHttpOnly = true;

    /**
     * Cookie SameSite policy: Strict, Lax, or None
     */
    private String cookieSameSite = "Lax";

    /**
     * Cookie max age in seconds
     * Default: 24 hours (86400 seconds)
     */
    private int cookieMaxAge = 86400;
}
