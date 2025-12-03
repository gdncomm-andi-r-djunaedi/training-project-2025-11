package com.gdn.training.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Member Service.
 * Located in infrastructure layer as per Clean Architecture guidelines.
 */
@SpringBootApplication(scanBasePackages = "com.gdn.training.member")
public class MemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }

}
