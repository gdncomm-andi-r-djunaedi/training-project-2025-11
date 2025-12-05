package com.blibli.member.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncryptor {
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }
}
