package com.marketplace.member.seeder;

import com.marketplace.member.entity.Member;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("seeder")
@Order(2) // Run after SystemConfigSeeder
@RequiredArgsConstructor
public class MemberSeeder implements CommandLineRunner {

    private static final int TOTAL_MEMBERS = 5000;
    private static final int BATCH_SIZE = 500;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final String[] firstNames = {
            "John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Olivia",
            "Robert", "Sophia", "William", "Isabella", "Joseph", "Mia", "Thomas",
            "Charlotte", "Charles", "Amelia", "Daniel", "Harper", "Matthew", "Evelyn",
            "Anthony", "Abigail", "Mark", "Emily", "Donald", "Elizabeth", "Steven",
            "Sofia", "Paul", "Avery", "Andrew", "Ella", "Joshua", "Scarlett", "Kenneth",
            "Grace", "Kevin", "Chloe", "Brian", "Victoria", "George", "Riley", "Timothy",
            "Aria", "Ronald", "Lily", "Edward", "Aurora"
    };

    private final String[] lastNames = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
            "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
            "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark",
            "Ramirez", "Lewis", "Robinson", "Walker", "Young", "Allen", "King",
            "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green",
            "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
            "Carter", "Roberts"
    };

    private final String[] domains = {
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "mail.com",
            "protonmail.com", "icloud.com", "aol.com"
    };

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        long existingCount = memberRepository.count();
        if (existingCount >= TOTAL_MEMBERS) {
            log.info("Database already has {} members. Skipping seeding.", existingCount);
            return;
        }

        log.info("Starting member seeding. Target: {} members", TOTAL_MEMBERS);
        
        String encodedPassword = passwordEncoder.encode("password123");
        int membersToCreate = TOTAL_MEMBERS - (int) existingCount;
        int created = 0;

        List<Member> batch = new ArrayList<>();

        for (int i = 0; i < membersToCreate; i++) {
            Member member = createRandomMember(i + (int) existingCount, encodedPassword);
            batch.add(member);

            if (batch.size() >= BATCH_SIZE) {
                memberRepository.saveAll(batch);
                created += batch.size();
                log.info("Created {} / {} members", created, membersToCreate);
                batch.clear();
            }
        }

        // Save remaining
        if (!batch.isEmpty()) {
            memberRepository.saveAll(batch);
            created += batch.size();
        }

        log.info("Member seeding completed. Total created: {}", created);
    }

    private Member createRandomMember(int index, String encodedPassword) {
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];
        String domain = domains[random.nextInt(domains.length)];
        String email = String.format("%s.%s%d@%s", 
                firstName.toLowerCase(), 
                lastName.toLowerCase(), 
                index, 
                domain);

        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(generatePhoneNumber())
                .address(generateAddress())
                .active(true)
                .build();
    }

    private String generatePhoneNumber() {
        return String.format("+62%d%d%d%d%d%d%d%d%d%d",
                random.nextInt(9) + 1,
                random.nextInt(10), random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10),
                random.nextInt(10), random.nextInt(10), random.nextInt(10));
    }

    private String generateAddress() {
        String[] streets = {"Main St", "Oak Ave", "Maple Dr", "Cedar Ln", "Pine Rd", "Elm Blvd"};
        String[] cities = {"Jakarta", "Surabaya", "Bandung", "Medan", "Semarang", "Makassar"};
        
        return String.format("%d %s, %s",
                random.nextInt(999) + 1,
                streets[random.nextInt(streets.length)],
                cities[random.nextInt(cities.length)]);
    }
}

