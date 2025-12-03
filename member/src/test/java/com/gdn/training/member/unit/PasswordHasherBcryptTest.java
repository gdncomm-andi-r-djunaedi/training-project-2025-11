package com.gdn.training.member.unit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import com.gdn.training.member.infrastructure.adapter.PasswordHasherBcrypt;
import com.gdn.training.member.domain.password.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasherBcryptTest {

    @Test
    public void testHashAndMatches() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        PasswordHasher passwordHasher = new PasswordHasherBcrypt(passwordEncoder);
        String password = "password";
        String hash = passwordHasher.hash(password);
        assertTrue(passwordHasher.matches(password, hash));
    }

}
