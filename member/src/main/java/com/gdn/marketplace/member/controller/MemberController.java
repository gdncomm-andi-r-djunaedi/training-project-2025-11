package com.gdn.marketplace.member.controller;

import com.gdn.marketplace.member.dto.AuthResponse;
import com.gdn.marketplace.member.dto.LoginRequest;
import com.gdn.marketplace.member.dto.RegisterRequest;
import com.gdn.marketplace.member.entity.Member;
import com.gdn.marketplace.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<Member> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(memberService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        memberService.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<Member> getProfile(Principal principal) {
        // Principal name is set by Spring Security (from JWT if we had a filter here,
        // but we don't yet in member service)
        // Wait, Member Service is behind Gateway. Gateway validates JWT.
        // But Member Service doesn't receive the JWT claims automatically unless we
        // forward them.
        // Or we can implement a simple JWT filter here too, OR trust the Gateway and
        // pass the user info in header.
        // The requirement says "login session validation via jwt cookie or jwt header".
        // Usually Gateway passes "X-Auth-User" header.
        // For now, let's assume we extract it from header or Principal if we configure
        // Resource Server.
        // Since I didn't configure Resource Server in Member Service, Principal might
        // be null.
        // I'll check "X-Auth-User" header or similar, OR I should implement a simple
        // filter in Member Service to populate SecurityContext from the token (which is
        // passed by Gateway).
        // Actually, Gateway passes the Authorization header. Member Service can
        // validate it again or just decode it.
        // Since they share the secret, Member Service can validate it.
        // I'll add a simple JwtRequestFilter in Member Service to populate Principal.

        // For now, I'll just return a placeholder or expect the username from a header
        // if I change the strategy.
        // But to be safe and use Spring Security properly, I should add the filter.
        // I'll implement a JwtRequestFilter in Member Service.

        return ResponseEntity.ok(memberService.getProfile(principal.getName()));
    }
}
