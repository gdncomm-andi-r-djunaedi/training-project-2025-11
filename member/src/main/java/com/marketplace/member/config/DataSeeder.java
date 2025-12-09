package com.marketplace.member.config;

import com.github.javafaker.Faker;
import com.marketplace.member.entity.Member;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test") // Don't run during tests
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${data.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${data.seed.members-count:5000}")
    private int membersCount;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled");
            return;
        }

        long existingCount = memberRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} members, skipping seeding", existingCount);
            return;
        }

        log.info("Starting to seed {} members...", membersCount);
        long startTime = System.currentTimeMillis();

        Faker faker = new Faker();
        List<Member> members = new ArrayList<>();

        // Create a few known test members first
        members.add(createTestMember("test@example.com", "testuser", "Test User"));
        members.add(createTestMember("admin@marketplace.com", "admin", "Admin User"));
        members.add(createTestMember("john@example.com", "johndoe", "John Doe"));

        // Generate remaining random members
        for (int i = 0; i < membersCount - 3; i++) {
            String username = faker.name().username() + i; // Add index to ensure uniqueness
            String email = username + "@example.com";
            String fullName = faker.name().fullName();

            members.add(createMember(email, username, fullName));

            // Save in batches of 1000 for better performance
            if (members.size() >= 1000) {
                memberRepository.saveAll(members);
                members.clear();
                log.info("Seeded {} members...", i + 4);
            }
        }

        // Save remaining members
        if (!members.isEmpty()) {
            memberRepository.saveAll(members);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Database seeding completed! Created {} members in {} seconds",
                membersCount, duration / 1000.0);
    }

    private Member createTestMember(String email, String username, String fullName) {
        // Test members have simple password: "Password123!"
        return createMemberWithPassword(email, username, fullName, "Password123!");
    }

    private Member createMember(String email, String username, String fullName) {
        // Random members have default password: "Pass123!"
        return createMemberWithPassword(email, username, fullName, "Pass123!");
    }

    private Member createMemberWithPassword(String email, String username, String fullName, String password) {
        return Member.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .build();
    }
}
