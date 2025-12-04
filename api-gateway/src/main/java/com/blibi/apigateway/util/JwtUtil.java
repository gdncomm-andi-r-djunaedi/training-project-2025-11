package com.blibi.apigateway.util;

import com.blibi.apigateway.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT utility class for generating and validating JWT tokens.
 * Supports both symmetric (HS256) and asymmetric (RS256) algorithms.
 * Generates OAuth-compatible JWT/JWS tokens with standard claims.
 * 
 * Topics to be learned: JWT, JWS, OAuth2, HMAC, RSA, Digital Signatures
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey symmetricKey;
    private PrivateKey rsaPrivateKey;
    private PublicKey rsaPublicKey;

    /**
     * Initialize keys based on configured algorithm
     */
    @PostConstruct
    public void init() {
        try {
            if ("HS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
                initSymmetricKey();
            } else if ("RS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
                initAsymmetricKeys();
            } else {
                throw new IllegalArgumentException("Unsupported algorithm: " + jwtProperties.getAlgorithm());
            }
            log.info("JWT initialized with algorithm: {}", jwtProperties.getAlgorithm());
        } catch (Exception e) {
            log.error("Failed to initialize JWT keys", e);
            throw new RuntimeException("JWT initialization failed", e);
        }
    }

    /**
     * Initialize symmetric key for HS256 algorithm
     */
    private void initSymmetricKey() {
        if (jwtProperties.getSecretKey() != null && !jwtProperties.getSecretKey().isEmpty()) {
            // Use provided secret key
            byte[] keyBytes = jwtProperties.getSecretKey().getBytes();
            this.symmetricKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("Using provided secret key for HS256");
        } else {
            // Generate random secret key
            this.symmetricKey = Jwts.SIG.HS256.key().build();
            log.warn("No secret key provided, generated random key. This key will change on restart!");
        }
    }

    /**
     * Initialize asymmetric keys for RS256 algorithm
     */
    private void initAsymmetricKeys() throws Exception {
        if (jwtProperties.getRsaPrivateKey() == null || jwtProperties.getRsaPublicKey() == null) {
            throw new IllegalArgumentException("RSA keys must be provided for RS256 algorithm");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // Parse private key
        String privateKeyPEM = jwtProperties.getRsaPrivateKey()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        this.rsaPrivateKey = keyFactory.generatePrivate(privateKeySpec);

        // Parse public key
        String publicKeyPEM = jwtProperties.getRsaPublicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        this.rsaPublicKey = keyFactory.generatePublic(publicKeySpec);

        log.info("RSA keys loaded successfully for RS256");
    }

    /**
     * Generate JWT token with OAuth-compatible claims
     * 
     * @param username Username (subject)
     * @return JWT token string
     */
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * Generate JWT token with OAuth-compatible claims and additional custom claims
     * 
     * @param username         Username (subject)
     * @param additionalClaims Additional custom claims to include
     * @return JWT token string
     */
    public String generateToken(String username, Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

        var builder = Jwts.builder()
                .subject(username) // sub: subject (username)
                .issuer(jwtProperties.getIssuer()) // iss: issuer
                .audience().add(jwtProperties.getAudience()).and() // aud: audience
                .issuedAt(now) // iat: issued at
                .expiration(expiration) // exp: expiration
                .claims(additionalClaims); // Additional custom claims

        // Sign with appropriate algorithm
        if ("HS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
            builder.signWith(symmetricKey, Jwts.SIG.HS256);
        } else if ("RS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
            builder.signWith(rsaPrivateKey, Jwts.SIG.RS256);
        }

        String token = builder.compact();
        log.debug("Generated JWT token for user: {}", username);
        return token;
    }

    /**
     * Validate JWT token and return username (subject)
     * 
     * @param token JWT token string
     * @return Username from token subject claim
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public String validateToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.getSubject();
    }

    /**
     * Validate JWT token and return all claims
     * 
     * @param token JWT token string
     * @return Claims object containing all token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public Claims validateAndGetClaims(String token) {
        var parserBuilder = Jwts.parser();

        // Verify with appropriate algorithm
        if ("HS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
            parserBuilder.verifyWith(symmetricKey);
        } else if ("RS256".equalsIgnoreCase(jwtProperties.getAlgorithm())) {
            parserBuilder.verifyWith(rsaPublicKey);
        }

        Claims claims = parserBuilder
                .build()
                .parseSignedClaims(token)
                .getPayload();

        log.debug("Validated JWT token for user: {}", claims.getSubject());
        return claims;
    }

    /**
     * Extract username from token without full validation
     * Useful for logging/debugging, but should not be used for authentication
     * 
     * @param token JWT token string
     * @return Username from token subject claim
     */
    public String extractUsername(String token) {
        try {
            return validateToken(token);
        } catch (Exception e) {
            log.warn("Failed to extract username from token", e);
            return null;
        }
    }

    /**
     * Check if token is expired
     * 
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateAndGetClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
