package com.example.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.example.member",
    "com.example.common"
})
public class MemberServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(MemberServiceApplication.class, args);
  }
}
