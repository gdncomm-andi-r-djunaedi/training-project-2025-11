package com.marketplace.seeder.command.impl;

import com.marketplace.member.entity.Member;
import com.marketplace.member.repository.MemberRepository;
import com.marketplace.seeder.command.SeedMembersCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedMembersCommandImpl implements SeedMembersCommand {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker;

    private static final int BATCH_SIZE = 500;
    private static final String DEFAULT_PASSWORD = "Password123!";

    @Override
    @Transactional
    public Integer execute(Integer targetCount) {
        long existingCount = memberRepository.count();

        if (existingCount >= targetCount) {
            log.info("Members already exist: {} (target: {}). Skipping seed.", existingCount, targetCount);
            return 0;
        }

        int membersToCreate = targetCount - (int) existingCount;
        log.info("Starting to seed {} members...", membersToCreate);

        String hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        Set<String> usedEmails = new HashSet<>();
        Set<String> usedPhoneNumbers = new HashSet<>();

        // Load existing emails and phone numbers to avoid duplicates
        memberRepository.findAll().forEach(member -> {
            usedEmails.add(member.getEmail());
            if (member.getPhoneNumber() != null) {
                usedPhoneNumbers.add(member.getPhoneNumber());
            }
        });

        List<Member> batch = new ArrayList<>(BATCH_SIZE);
        int createdCount = 0;
        int batchNumber = 1;

        for (int i = 0; i < membersToCreate; i++) {
            Member member = generateUniqueMember(hashedPassword, usedEmails, usedPhoneNumbers);
            batch.add(member);

            if (batch.size() >= BATCH_SIZE) {
                memberRepository.saveAll(batch);
                createdCount += batch.size();
                log.info("Batch {} completed. Progress: {}/{}", batchNumber++, createdCount, membersToCreate);
                batch.clear();
            }
        }

        // Save remaining members
        if (!batch.isEmpty()) {
            memberRepository.saveAll(batch);
            createdCount += batch.size();
            log.info("Final batch completed. Total created: {}", createdCount);
        }

        log.info("Successfully seeded {} members", createdCount);
        return createdCount;
    }

    private Member generateUniqueMember(String hashedPassword, Set<String> usedEmails, Set<String> usedPhoneNumbers) {
        String email = generateUniqueEmail(usedEmails);
        String phoneNumber = generateUniquePhoneNumber(usedPhoneNumbers);

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        return Member.builder()
                .email(email)
                .passwordHash(hashedPassword)
                .fullName(faker.name().fullName())
                .address(faker.address().fullAddress())
                .phoneNumber(phoneNumber)
                .roles(roles)
                .build();
    }

    private String generateUniqueEmail(Set<String> usedEmails) {
        String email;
        int attempts = 0;
        do {
            String firstName = faker.name().firstName().toLowerCase().replaceAll("[^a-z]", "");
            String lastName = faker.name().lastName().toLowerCase().replaceAll("[^a-z]", "");
            String randomNum = String.valueOf(faker.number().numberBetween(1, 9999));
            email = firstName + "." + lastName + randomNum + "@" + faker.internet().domainName();
            attempts++;
        } while (usedEmails.contains(email) && attempts < 100);

        usedEmails.add(email);
        return email;
    }

    private String generateUniquePhoneNumber(Set<String> usedPhoneNumbers) {
        String phoneNumber;
        int attempts = 0;
        do {
            phoneNumber = faker.phoneNumber().cellPhone();
            attempts++;
        } while (usedPhoneNumbers.contains(phoneNumber) && attempts < 100);

        usedPhoneNumbers.add(phoneNumber);
        return phoneNumber;
    }
}

