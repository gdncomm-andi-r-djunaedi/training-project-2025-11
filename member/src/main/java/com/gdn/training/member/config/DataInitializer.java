package com.gdn.training.member.config;

import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing member data...");

        // 1. Remove older data
        memberRepository.deleteAll();
        log.info("Cleared existing member data.");

        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String sequence = String.format("%05d", i);
            String username = "TESTUSER" + sequence;
            String password = "P@ssword" + sequence;
            String email = "testmail" + sequence + "@test.com";
            String firstName = "user-test";
            String lastName = sequence;

            Member member = Member.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();
            members.add(member);
        }

        memberRepository.saveAll(members);
        log.info("Inserted {} sample members.", members.size());
    }
}
