package com.dev.onlineMarketplace.MemberService.config;

import com.dev.onlineMarketplace.MemberService.entity.MemberEntity;
import com.dev.onlineMarketplace.MemberService.repository.MemberRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataSeederConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSeederConfig.class);
    private static final int TOTAL_MEMBERS = 5000;
    private static final int BATCH_SIZE = 100;

    @Bean
    public CommandLineRunner seedDatabase(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists
            long existingCount = memberRepository.count();
            if (existingCount >= TOTAL_MEMBERS) {
                logger.info("Database already contains {} members. Skipping seeding.", existingCount);
                return;
            }

            logger.info("Starting data seeding process for {} members...", TOTAL_MEMBERS);
            long startTime = System.currentTimeMillis();

            Faker faker = new Faker();
            Set<String> usedEmails = new HashSet<>();
            Set<String> usedUsernames = new HashSet<>();

            // Load existing emails and usernames to avoid duplicates
            memberRepository.findAll().forEach(member -> {
                usedEmails.add(member.getEmail());
                usedUsernames.add(member.getUsername());
            });

            int seededCount = 0;
            int batchCount = 0;

            while (seededCount < TOTAL_MEMBERS) {
                // Generate unique email and username
                String email;
                String username;

                do {
                    String firstName = faker.name().firstName().toLowerCase().replaceAll("[^a-z]", "");
                    String lastName = faker.name().lastName().toLowerCase().replaceAll("[^a-z]", "");
                    int randomNum = faker.number().numberBetween(1, 9999);

                    email = firstName + "." + lastName + randomNum + "@" + faker.internet().domainName();
                    username = firstName + lastName + randomNum;
                } while (usedEmails.contains(email) || usedUsernames.contains(username));

                usedEmails.add(email);
                usedUsernames.add(username);

                // Create member entity
                MemberEntity member = new MemberEntity();
                member.setUsername(username);
                member.setEmail(email);
                // Use a common password for all seeded members: "Password123"
                member.setPassword(passwordEncoder.encode("Password123"));

                memberRepository.save(member);
                seededCount++;
                batchCount++;

                // Log progress every batch
                if (batchCount >= BATCH_SIZE) {
                    logger.info("Seeded {} / {} members...", seededCount, TOTAL_MEMBERS);
                    batchCount = 0;
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            logger.info("✅ Successfully seeded {} members in {} seconds", TOTAL_MEMBERS, duration);
            logger.info("📧 All seeded members use password: 'Password123'");
            logger.info("💡 Example login: username='{}', password='Password123'",
                    memberRepository.findAll().get(0).getUsername());
        };
    }
}
