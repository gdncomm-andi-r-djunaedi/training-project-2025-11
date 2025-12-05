package com.gdn.marketplace.member.service;

import com.gdn.marketplace.member.dto.AuthRequest;
import com.gdn.marketplace.member.entity.Member;
import com.gdn.marketplace.member.repository.MemberRepository;
import com.gdn.marketplace.member.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String saveMember(AuthRequest authRequest) {
        Member member = new Member();
        member.setUsername(authRequest.getUsername());
        member.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        member.setEmail(authRequest.getEmail());
        repository.save(member);
        return "User added to the system";
    }

    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);
    }
    
    public boolean validateUser(AuthRequest authRequest) {
        return repository.findByUsername(authRequest.getUsername())
                .map(member -> passwordEncoder.matches(authRequest.getPassword(), member.getPassword()))
                .orElse(false);
    }
}
