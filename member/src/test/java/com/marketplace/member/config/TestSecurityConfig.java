package com.marketplace.member.config;

import com.marketplace.common.security.JwtAuthenticationFilter;
import com.marketplace.common.security.JwtTokenProvider;
import com.marketplace.common.security.RsaKeyProperties;
import com.marketplace.common.security.TokenBlacklistService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Test security configuration for member service integration tests.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/members/register",
            "/api/members/login",
            "/actuator/**",
            "/swagger-ui/**",
            "/api-docs/**"
    );

    private static final String TEST_PUBLIC_KEY = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnUcdaQBpzumEFFSyPuhZ
            bmo4AwnRsi+1RwbzlSmkPa3FXRCuHduYXtp1wQcIk+ICzC02sengnSFP3lLrPz3s
            co3ch2f/9q6ulkiF3VU8wQmSIXd04PqY7E2nBjF4gYCteWrtQ1qP5VVPBwMVQRsZ
            E3uMQZuIOUp/9ppfVtgMydGGJHSqSdE/dq/3ZWhkBGL/ipEYkuoyKbF/hYetfkVK
            5t+fJsHwrATAzFo8FCe/OyLUOF8ZPrEE53efUiwAgN54/76FaewmpWgJg9MRsfGA
            TuwVxR5EOlJ+MTNPUljr7SvntQ68LijYjpE5FzsFUXm6HZjdKxwLDEE/vjMNz4NR
            rwIDAQAB
            -----END PUBLIC KEY-----
            """;

    private static final String TEST_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCdRx1pAGnO6YQU
            VLI+6FluajgDCdGyL7VHBvOVKaQ9rcVdEK4d25he2nXBBwiT4gLMLTax6eCdIU/e
            Uus/PexyjdyHZ//2rq6WSIXdVTzBCZIhd3Tg+pjsTacGMXiBgK15au1DWo/lVU8H
            AxVBGxkTe4xBm4g5Sn/2ml9W2AzJ0YYkdKpJ0T92r/dlaGQEYv+KkRiS6jIpsX+F
            h61+RUrm358mwfCsBMDMWjwUJ787ItQ4Xxk+sQTnd59SLACj3nj/voVp7CalaAmD
            0xGx8YBO7BXFHkQ6Un4xM09SWOvtK+e1DrwuKNiOkTkXOwVRebodmN0rHAsMQT++
            Mw3Pg1GvAgMBAAECggEADw4UMTlo5Iq2NhkNv+7dSuKyoW0h3kpHyWb9aaNxY9qH
            ThB3V0gvFJBnNaKcB47rVb3N6D4t0sUiGWfD4T0YL6JKJTy9Fy3ohWFQ7x3p0q3z
            g9sKFPJRu6uhFi7j1O1xBgkpmH+w3KCh9PxCT9w2Y0SZq8+Ib7F3WvBWFxbmH4V5
            cC3YWb5hG8ywKa3+xEaD8Y7LyUGKGT0dXEFwP5s0vVJL1q3yKR7M1KuhK7UqN7xW
            9F0WcQGwHFaK7Ls+YvLb3HKJd2G4v3f7NQP6hR9/w1GIE6Tz8FiW+k1B8Ux3eZlb
            Q4nC7dLK8p7Uy+MfHn0A3F2j4T+2wLjE5OdFw8p9QQKBgQDPVIvHy4kN7gE3DLGC
            m3wUKNWuiQ3m+e7g0iH3VLCPLdj7Q/w6d4sS0Wsh5r4D0DWJE/f8k4e1T8iJ8tLm
            9NjMjS9/qmQ+RH7nFf+5q7Q9z7W8J9B5j3vYzMq9LwL7rVdFN/j8D6n4C8L5j3vY
            zMq9LwL7rVdFN/j8D6n4C8KJjwKBgQDCXJFV2K8l8j0lQ4w5L9J8z7W8J9B5j3vY
            zMq9LwL7rVdFN/j8D6n4C8L5j3vYzMq9LwL7rVdFN/j8D6n4C8KJj3vYzMq9LwL7
            rVdFN/j8D6n4C8L5j3vYzMq9LwL7rVdFN/j8D6n4C8KJj3vYzMq9LwL7rVdFN/j8
            D6n4C8KJjwKBgQCwXJFV2K8l8j0lQ4w5L9J8z7W8J9B5j3vYzMq9LwL7rVdFN/j8
            D6n4C8L5j3vYzMq9LwL7rVdFN/j8D6n4C8KJj3vYzMq9LwL7rVdFN/j8D6n4C8L5
            j3vYzMq9LwL7rVdFN/j8D6n4C8KJj3vYzMq9LwL7rVdFN/j8D6n4C8KJjwKBgADX
            JFV2K8l8j0lQ4w5L9J8z7W8J9B5j3vYzMq9LwL7rVdFN/j8D6n4C8L5j3vYzMq9L
            wL7rVdFN/j8D6n4C8KJj3vYzMq9LwL7rVdFN/j8D6n4C8L5j3vYzMq9LwL7rVdFN
            /j8D6n4C8KJj3vYzMq9LwL7rVdFN/j8D6n4C8KJjAoGBAMXJFV2K8l8j0lQ4w5L9
            J8z7W8J9B5j3vYzMq9LwL7rVdFN/j8D6n4C8L5j3vYzMq9LwL7rVdFN/j8D6n4C8
            KJj3vYzMq9LwL7rVdFN/j8D6n4C8L5j3vYzMq9LwL7rVdFN/j8D6n4C8KJj3vYzM
            q9LwL7rVdFN/j8D6n4C8KJjA
            -----END PRIVATE KEY-----
            """;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public RsaKeyProperties testRsaKeyProperties() {
        return new RsaKeyProperties(TEST_PUBLIC_KEY, TEST_PRIVATE_KEY);
    }

    @Bean
    @Primary
    public JwtTokenProvider testJwtTokenProvider(RsaKeyProperties rsaKeyProperties) {
        return new JwtTokenProvider(rsaKeyProperties, 3600000L, 86400000L);
    }

    @Bean
    @Primary
    public TokenBlacklistService testTokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        return new TokenBlacklistService(redisTemplate);
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter testJwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService, PUBLIC_ENDPOINTS);
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/register", "/api/members/login").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
