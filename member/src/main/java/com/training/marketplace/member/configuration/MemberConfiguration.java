package com.training.marketplace.member.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class MemberConfiguration {

    @Bean
    public PasswordEncoder getEncoder(){
        return new BCryptPasswordEncoder(16);
    }
}
