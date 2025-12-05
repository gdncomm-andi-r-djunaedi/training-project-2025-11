package com.dev.onlineMarketplace.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.private-key-path:classpath:keys/private_key.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.keys.directory:src/main/resources/keys}")
    private String keysDirectory;

    @Value("${jwt.access-token.expiration:30000000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        // Load public key for validation
        String publicKeyContent = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        publicKeyContent = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(spec);

        // Try to load private key for token generation
        try {
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
        } catch (Exception e) {
            logger.warn("Could not load/generate private key. Token generation will not work. Error: {}", e.getMessage());
        }
    }

    private void generateKeyPair(File privateKeyFile, File publicKeyFile) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        this.privateKey = keyPair.getPrivate();
        // Update public key from generated pair
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
        if (privateKey == null) {
            throw new IllegalStateException("Private key not available. Cannot generate tokens.");
        }
        return generateToken(username, accessTokenExpiration, "ACCESS");
    }

    public String generateRefreshToken(String username) {
        if (privateKey == null) {
            throw new IllegalStateException("Private key not available. Cannot generate tokens.");
        }
        return generateToken(username, refreshTokenExpiration, "REFRESH");
    }

    private String generateToken(String username, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("type", tokenType)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(privateKey, io.jsonwebtoken.SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
