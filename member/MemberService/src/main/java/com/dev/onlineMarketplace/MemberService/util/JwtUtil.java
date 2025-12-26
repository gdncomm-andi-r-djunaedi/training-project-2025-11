package com.dev.onlineMarketplace.MemberService.util;

import com.dev.onlineMarketplace.MemberService.exception.InvalidTokenException;
import com.dev.onlineMarketplace.MemberService.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.keys.directory}")
    private String keysDirectory;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        File keyDir = new File(keysDirectory);
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }

        File privateKeyFile = new File(keyDir, "private_key.der");
        File publicKeyFile = new File(keyDir, "public_key.der");

        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            logger.info("Generating new RSA key pair...");
            generateKeyPair(privateKeyFile, publicKeyFile);
        } else {
            logger.info("Loading existing RSA key pair...");
            loadKeyPair(privateKeyFile, publicKeyFile);
        }
    }

    private void generateKeyPair(File privateKeyFile, File publicKeyFile) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        // Save keys to files
        try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
            fos.write(privateKey.getEncoded());
        }

        try (FileOutputStream fos = new FileOutputStream(publicKeyFile)) {
            fos.write(publicKey.getEncoded());
        }

        logger.info("RSA key pair generated and saved successfully");
    }

    private void loadKeyPair(File privateKeyFile, File publicKeyFile) throws Exception {
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        this.privateKey = keyFactory.generatePrivate(privateKeySpec);

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);

        logger.info("RSA key pair loaded successfully");
    }

    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration, "ACCESS");
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration, "REFRESH");
    }

    private String generateToken(String username, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Token expired: {}", e.getMessage());
            throw new TokenExpiredException("Token expired");
        } catch (SignatureException e) {
            logger.error("Invalid token signature: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token signature");
        } catch (MalformedJwtException e) {
            logger.error("Malformed token: {}", e.getMessage());
            throw new InvalidTokenException("Malformed token");
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
