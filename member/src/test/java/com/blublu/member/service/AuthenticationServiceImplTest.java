package com.blublu.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blublu.member.exception.UsernameNotExistException;
import com.blublu.member.exception.WrongPasswordException;
import com.blublu.member.interfaces.MemberService;
import com.blublu.member.model.request.LoginRequest;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    AuthenticationServiceImpl authenticationService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    MemberService memberService;

    @Mock
    CustomUserDetailsService customUserDetailsService;

    @Test
    void testAuthenticateUser_Success() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testUser")
                .password("password")
                .build();

        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());

        when(memberService.isUsernameExist("testUser")).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername("testUser")).thenReturn(userDetails);

        UserDetails result = authenticationService.authenticateUser(loginRequest);

        assertEquals(userDetails, result);
        verify(memberService).isUsernameExist("testUser");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(customUserDetailsService).loadUserByUsername("testUser");
    }

    @Test
    void testAuthenticateUser_UsernameNotExist() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("unknownUser")
                .password("password")
                .build();

        when(memberService.isUsernameExist("unknownUser")).thenReturn(false);

        assertThrows(UsernameNotExistException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(memberService).isUsernameExist("unknownUser");
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testUser")
                .password("wrongPassword")
                .build();

        when(memberService.isUsernameExist("testUser")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(WrongPasswordException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });

        verify(memberService).isUsernameExist("testUser");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
