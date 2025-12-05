package com.training.member.memberassignment.seeder;
import com.training.member.memberassignment.entity.MemberEntity;
import com.training.member.memberassignment.repository.MemberRepository;
import com.github.javafaker.Faker;
import com.training.member.memberassignment.config.MemberSeederProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemberSeeder {

    private static final Logger log = LoggerFactory.getLogger(MemberSeeder.class);

    private final MemberRepository memberRepository;
    private final MemberSeederProperties memberSeederProperties;
    private final Faker faker = new Faker();

    public MemberSeeder(MemberRepository repo, MemberSeederProperties memberSeederProperties) {
        this.memberRepository = repo;
        this.memberSeederProperties = memberSeederProperties;
    }

    public void seed() {

        if (!memberSeederProperties.isEnabled()) {
            log.info("⏭ Member seeding is disabled — skipping.");
            return;
        }
        long existing = memberRepository.count();
        int max = memberSeederProperties.getMaxCount();
        log.info(" Existing members: {}", existing);
        log.info(" Target max members: {}", max);
        if (existing >= max) {
            log.info("No seeding needed. Database already has {} members.", existing);
            return;
        }
        int toInsert = max - (int) existing;
        log.info("Starting seeding of {} new members...", toInsert);
        List<MemberEntity> members = new ArrayList<>();
        for (int i = 0; i < toInsert; i++) {
            MemberEntity member = new MemberEntity();
            member.setEmail("user_" + UUID.randomUUID() + "@example.com");
            member.setPasswordHash(faker.internet().password());
            members.add(member);
            if (i > 0 && i % 100 == 0) {
                log.info(" Prepared {} members so far...", i);
            }
        }
        memberRepository.saveAll(members);
        log.info("Successfully inserted {} new members.", toInsert);
    }
}
