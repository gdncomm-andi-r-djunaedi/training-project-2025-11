package com.blublu.member.util;

import com.blublu.member.entity.Member;
import com.blublu.member.properties.MemberProperties;
import com.blublu.member.repository.MemberRepository;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FakeDataGenerator {

  private final Faker faker = new Faker();
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private MemberProperties memberProperties;

  @PostConstruct
  public void generateData() {
    if (Boolean.parseBoolean(memberProperties.getFlag().get("auto-generate-data"))) {
      long count = Integer.parseInt(memberProperties.getFlag().getOrDefault("auto-generate-data-count", "10"));
      generateRealisticMember(count);
    }
  }

  private void generateRealisticMember(long count) {
    log.info("Generating realistic member data");
    Map<String, String> userPassword = new HashMap<>();
    List<Member> members = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      String username = faker.name().username();
      String password = faker.internet().password(5, 10, true);
      userPassword.put(username, password);
      Member member = Member.builder().username(username).password(passwordEncoder.encode(password)).build();
      members.add(member);
    }
    memberRepository.saveAll(members);
    log.info("u & p combo: {}", userPassword);
  }
}
