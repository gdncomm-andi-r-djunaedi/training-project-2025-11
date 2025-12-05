package com.ecommerce.member.service;

import com.ecommerce.member.config.JwtUtils;
import com.ecommerce.member.dto.AuthRequest;
import com.ecommerce.member.dto.AuthResponse;
import com.ecommerce.member.dto.RegisterRequest;
import com.ecommerce.member.entity.Member;
import com.ecommerce.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public AuthResponse authenticateUser(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        Member member = memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        return new AuthResponse(jwt, member.getUsername(), member.getId());
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (memberRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (memberRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        Member member = new Member();
        member.setUsername(registerRequest.getUsername());
        member.setEmail(registerRequest.getEmail());
        member.setPassword(encoder.encode(registerRequest.getPassword()));

        memberRepository.save(member);
    }
}
