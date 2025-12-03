package com.gdn.training.member.infrastructure.adapter;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.gdn.training.member.domain.password.PasswordHasher;

@Component
public class PasswordHasherBcrypt implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public PasswordHasherBcrypt(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }
}
