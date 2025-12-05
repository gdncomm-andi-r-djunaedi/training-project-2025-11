package com.ecommerce.member.config;

import com.ecommerce.member.entity.Member;
import com.ecommerce.member.repository.MemberRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.count() == 0) {
            Faker faker = new Faker();
            List<Member> members = new ArrayList<>();
            String encodedPassword = passwordEncoder.encode("password"); // Default password for all

            for (int i = 0; i < 5000; i++) {
                Member member = new Member();
                member.setUsername(faker.name().username() + i); // Ensure uniqueness
                member.setEmail(faker.internet().emailAddress() + i); // Ensure uniqueness
                member.setPassword(encodedPassword);
                members.add(member);
            }

            memberRepository.saveAll(members);
            System.out.println("Seeded 5000 members");
        }
    }
}
