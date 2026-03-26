package com.MarketPlace.MemberService.hashPassword;

import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class PasswordHashUtil {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Generates a cryptographically strong, random salt.
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes is standard for salt
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes the password using the PBKDF2 algorithm.
     * Returns a string in the format: [Salt (Base64)]:[Hash (Base64)]
     */
    public static String hashPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

        byte[] hash = factory.generateSecret(spec).getEncoded();

        String saltStr = Base64.getEncoder().encodeToString(salt);
        String hashStr = Base64.getEncoder().encodeToString(hash);

        return saltStr + ":" + hashStr;
    }

    /**
     * Verifies an input password against a stored hash+salt string.
     */
    public static boolean verifyPassword(String password, String storedHashAndSalt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        String[] parts = storedHashAndSalt.split(":");
        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String storedHash = parts[1];

        String newHashAndSalt = hashPassword(password, salt);
        String newHash = newHashAndSalt.split(":")[1];

        return newHash.equals(storedHash);
    }

}
