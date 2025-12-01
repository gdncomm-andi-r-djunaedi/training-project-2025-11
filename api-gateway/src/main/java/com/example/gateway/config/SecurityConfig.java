package com.example.gateway.config;

import com.example.common.security.JwtService;
import com.example.gateway.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public JwtService jwtService() {
    // TODO: move this secret to config later
    return new JwtService("change-this-secret-change-this-secret-1234");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // Swagger / docs open
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // Member auth endpoints (register/login) open
            .requestMatchers("/api/members/register", "/api/members/login").permitAll()

            // Product browse endpoints open
            .requestMatchers("/api/products/**").permitAll()

            // Cart + logout require auth
            .requestMatchers("/api/cart/**", "/api/members/logout").authenticated()

            // Anything else: for now permitAll (you can tighten later)
            .anyRequest().permitAll())
        .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }
}