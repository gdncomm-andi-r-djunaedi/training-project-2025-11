package com.dev.onlineMarketplace.MemberService.controller;

import com.dev.onlineMarketplace.MemberService.dto.GDNResponseData;
import com.dev.onlineMarketplace.MemberService.entity.MemberEntity;
import com.dev.onlineMarketplace.MemberService.repository.MemberRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/member")
@Tag(name = "Member Management", description = "APIs for member list and search")
public class MemberSearchController {

    private static final Logger logger = LoggerFactory.getLogger(MemberSearchController.class);

    private final MemberRepository memberRepository;

    public MemberSearchController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/members")
    public ResponseEntity<GDNResponseData<Map<String, Object>>> getAllMembers() {
        logger.info("GET /api/v1/member/members - Fetching all members");

        List<MemberEntity> members = memberRepository.findAll();

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", members.size());
        result.put("members", members.stream().map(m -> {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("id", m.getId());
            memberInfo.put("username", m.getUsername());
            memberInfo.put("email", m.getEmail());
            memberInfo.put("passwordHash",
                    m.getPassword().substring(0, Math.min(20, m.getPassword().length())) + "...");
            return memberInfo;
        }).toList());

        return ResponseEntity.ok(GDNResponseData.success(result, "Members fetched successfully"));
    }

    @GetMapping("/members/search")
    public ResponseEntity<GDNResponseData<Map<String, Object>>> searchMember(@RequestParam String query) {
        logger.info("GET /api/v1/member/members/search?query={}", query);

        Map<String, Object> result = new HashMap<>();

        var byUsername = memberRepository.findByUsername(query);
        var byEmail = memberRepository.findByEmail(query);

        result.put("searchQuery", query);
        result.put("foundByUsername", byUsername.isPresent());
        result.put("foundByEmail", byEmail.isPresent());

        if (byUsername.isPresent()) {
            MemberEntity m = byUsername.get();
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("id", m.getId());
            memberInfo.put("username", m.getUsername());
            memberInfo.put("email", m.getEmail());
            result.put("memberByUsername", memberInfo);
        }

        if (byEmail.isPresent()) {
            MemberEntity m = byEmail.get();
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("id", m.getId());
            memberInfo.put("username", m.getUsername());
            memberInfo.put("email", m.getEmail());
            result.put("memberByEmail", memberInfo);
        }

        return ResponseEntity.ok(GDNResponseData.success(result, "Search completed"));
    }
}
