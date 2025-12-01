package com.example.member.config;

import com.example.member.domain.Member;
import com.example.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class MemberDataLoader {

  private static final Logger log = LoggerFactory.getLogger(MemberDataLoader.class);

  private final MemberRepository repo;
  private final PasswordEncoder encoder;

  @Bean
  CommandLineRunner initMembers() {
    return args -> {
      if (repo.count() > 0) {
        return;
      }
      log.info("Seeding 5,000 demo members");
      for (int i = 1; i <= 5000; i++) {
        Member m = Member.builder()
            .email("user" + i + "@example.com")
            .fullName("User " + i)
            .passwordHash(encoder.encode("password"))
            .build();
        repo.save(m);
      }
      log.info("Finished seeding members");
    };
  }
}
