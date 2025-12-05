package com.example.memberservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberServiceApplication {

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner commandLineRunner(org.springframework.security.crypto.password.PasswordEncoder encoder) {
        return args -> {
            System.out.println("GENERATED_HASH: " + encoder.encode("password123"));
        };
    }

	public static void main(String[] args) {
		SpringApplication.run(MemberServiceApplication.class, args);
	}

}
