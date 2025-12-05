package com.gdn.training.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import com.gdn.training.member.model.request.AuthenticationRequest;
import com.gdn.training.member.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void authenticateUserWithValidCredentials_Success() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("testing")
                .build();

        String expectedToken = "jwt-token-12345";
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateToken(request.getEmail())).thenReturn(expectedToken);

        var result = authenticationService.authenticate(request);

        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(request.getEmail());
    }

    @Test
    void authenticateUserWithInvalidCredentials_ThrowsAuthenticationException() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("testing")
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.authenticate(request);
        });

        assertEquals("Invalid email or password", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(0)).generateToken(any());
    }

    @Test
    void logoutUser_Success() {
        String token = "jwt-token-12345";
        Date futureExpiration = new Date(System.currentTimeMillis() + 3600 * 1000);

        when(jwtService.extractExpiration(token)).thenReturn(futureExpiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authenticationService.logout(token);

        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                eq("blacklist:" + token),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void logoutUserTtl0_Success() {
        String token = "jwt-token-12345";
        when(jwtService.extractExpiration(token)).thenReturn(new Date(System.currentTimeMillis()));
        authenticationService.logout(token);
        verify(redisTemplate, times(0)).opsForValue();
    }

    @Test
    void logoutUser_ThrowsException() {
        String token = "jwt-token-12345";
        when(jwtService.extractExpiration(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        authenticationService.logout(token);
        verify(redisTemplate, times(0)).opsForValue();
    }

}
