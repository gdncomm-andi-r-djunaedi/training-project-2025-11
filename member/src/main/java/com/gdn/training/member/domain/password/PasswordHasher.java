package com.gdn.training.member.domain.password;

public interface PasswordHasher {
    String hash(String password);

    boolean matches(String password, String hash);
}
