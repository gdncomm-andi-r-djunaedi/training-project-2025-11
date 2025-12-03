package com.microservice.member.seeder;

import com.microservice.member.entity.Member;
import com.microservice.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MemberSeederService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public MemberSeederService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // returns how many members in DB after seeding
    @Transactional
    public long seedMembersIfNeeded(int targetCount) {

        long existing = memberRepository.count();
        if (existing >= targetCount) {
            return existing;
        }

        List<Member> batch = new ArrayList<>();

        for (int i = (int) existing + 1; i <= targetCount; i++) {
            Member m = new Member();
            m.setName("User" + i);
            m.setEmail("user" + i + "@example.com");
            m.setPhoneNumber("900000" + String.format("%04d", i)); // unique
            m.setAddress("Address " + i);
            m.setCreatedAt(new Date());
            m.setPasswordHash(encoder.encode("Password@" + i)); // SAME ENCODER

            batch.add(m);

            if (batch.size() == 500) {
                memberRepository.saveAll(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            memberRepository.saveAll(batch);
        }

        return memberRepository.count();
    }
}
