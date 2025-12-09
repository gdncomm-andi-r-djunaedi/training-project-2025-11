package com.gdn.training.member.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gdn.training.member.entity.Member;
import com.gdn.training.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;
    private final String secret = "testkey"; // Should be in properties/env

    public JwtFilter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secret))
                        .build()
                        .verify(token);

                String username = jwt.getSubject();

                Member member = memberRepository.findByUsername(username).orElse(null);
                if (member != null && member.getLastLogout() != null) {
                    System.out.println("Token Issued At: " + jwt.getIssuedAt());
                    System.out.println("Member Last Logout: " + member.getLastLogout());
                    if (jwt.getIssuedAt().before(member.getLastLogout())) {
                        System.out.println("Token expired!");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                System.out.println("Token verification failed: " + e.getMessage());
                // Invalid token
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
