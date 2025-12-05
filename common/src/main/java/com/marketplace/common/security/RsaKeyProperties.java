package com.marketplace.common.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Getter
@Component
public class RsaKeyProperties {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public RsaKeyProperties(
            @Value("${jwt.rsa.public-key}") String publicKeyString,
            @Value("${jwt.rsa.private-key:}") String privateKeyString) {
        
        this.publicKey = parsePublicKey(publicKeyString);
        this.privateKey = privateKeyString.isEmpty() ? null : parsePrivateKey(privateKeyString);
        
        log.info("RSA keys loaded successfully. Private key present: {}", privateKey != null);
    }

    private RSAPublicKey parsePublicKey(String key) {
        try {
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key", e);
        }
    }

    private RSAPrivateKey parsePrivateKey(String key) {
        try {
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key", e);
        }
    }

    public boolean hasPrivateKey() {
        return privateKey != null;
    }
}

