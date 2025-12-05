package com.ecommerce.gateway.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(@Value("${jwt.private-key-location}") Resource privateKeyResource) {
        try {
            KeyComponents keyComponents = initializeKeys(privateKeyResource);
            this.jwtEncoder = keyComponents.encoder;
            this.jwtDecoder = NimbusJwtDecoder.withPublicKey(keyComponents.publicKey).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JwtService", e);
        }
    }

    private KeyComponents initializeKeys(Resource privateKeyResource) throws Exception {
        String keyContent = StreamUtils.copyToString(privateKeyResource.getInputStream(), StandardCharsets.UTF_8);
        keyContent = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(spec);

        // Derive public key from private key
        RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) privateKey;
        java.security.spec.RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(
                crtKey.getModulus(), crtKey.getPublicExponent());
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec);

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("ecommerce-key")
                .build();

        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        JwtEncoder encoder = new NimbusJwtEncoder(jwks);

        return new KeyComponents(encoder, publicKey);
    }

    private static class KeyComponents {
        final JwtEncoder encoder;
        final RSAPublicKey publicKey;

        KeyComponents(JwtEncoder encoder, RSAPublicKey publicKey) {
            this.encoder = encoder;
            this.publicKey = publicKey;
        }
    }

    public String generateToken(String username, Long userId) {
        Instant now = Instant.now();
        long expiry = 3600L; // 1 hour

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(username)
                .claim("userId", userId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }
}
