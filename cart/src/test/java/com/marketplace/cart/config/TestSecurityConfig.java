package com.marketplace.cart.config;

import com.marketplace.common.security.MemberContext;
import com.marketplace.common.security.MemberContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Test security configuration that extracts member ID from X-Member-Id header
 * for easier testing without actual JWT tokens.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(testAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public OncePerRequestFilter testAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            FilterChain filterChain) throws ServletException, IOException {
                String memberId = request.getHeader("X-Member-Id");
                
                if (memberId != null && !memberId.isEmpty()) {
                    try {
                        UUID uuid = UUID.fromString(memberId);
                        
                        // Set MemberContext
                        MemberContext context = MemberContext.builder()
                                .memberId(uuid)
                                .email("test@example.com")
                                .token("test-token")
                                .build();
                        MemberContextHolder.setContext(context);
                        
                        // Set Spring Security Authentication
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(uuid, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (Exception e) {
                        // Invalid member ID format
                    }
                }
                
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    MemberContextHolder.clearContext();
                    SecurityContextHolder.clearContext();
                }
            }
        };
    }
}
