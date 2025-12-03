package com.microservice.member.controller;

import com.microservice.member.seeder.MemberSeederService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dev")
public class DevSeedController {

    private final MemberSeederService seederService;

    public DevSeedController(MemberSeederService seederService) {
        this.seederService = seederService;
    }

    // Call this once to create members
    @PostMapping("/seed-members")
    public ResponseEntity<String> seedMembers() {
        long total = seederService.seedMembersIfNeeded(5000);
        return ResponseEntity.ok("Members present in DB: " + total);
    }
}

