package com.gdn.marketplace.member.config;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.repository.MemberRepository;
import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private MemberService service;

    @Autowired
    private MemberRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            for (int i = 1; i <= 5000; i++) {
                AuthRequest request = new AuthRequest("user" + i, "password" + i, "user" + i + "@example.com");
                service.saveMember(request);
            }
            System.out.println("Seeded 5000 members");
        }
    }
}
