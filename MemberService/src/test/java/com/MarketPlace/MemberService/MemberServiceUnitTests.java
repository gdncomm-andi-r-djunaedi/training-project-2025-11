package com.MarketPlace.MemberService;

import com.MarketPlace.MemberService.dto.MemberDetailDto;
import com.MarketPlace.MemberService.dto.MemberLoginRequestDTO;
import com.MarketPlace.MemberService.dto.MemberLoginResponseDTO;
import com.MarketPlace.MemberService.dto.MemberResponseDTO;
import com.MarketPlace.MemberService.entity.Member;
import com.MarketPlace.MemberService.exceptions.MemberServiceException;
import com.MarketPlace.MemberService.repository.MemberRepository;
import com.MarketPlace.MemberService.hashPassword.PasswordHashUtil;
import com.MarketPlace.MemberService.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MemberServiceUnitTests {
    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    //  REGISTER  
    @Test
    void register_Success() throws NoSuchAlgorithmException, InvalidKeySpecException {
        MemberResponseDTO request = new MemberResponseDTO();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Member savedMember = new Member(1L, "john", PasswordHashUtil.hashPassword("password123", PasswordHashUtil.generateSalt()), "john@example.com");

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        MemberResponseDTO response = memberService.register(request);

        assertNotNull(response);
        assertEquals("john", response.getUsername());
        assertEquals("john@example.com", response.getEmail());

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void register_NullRequest_ThrowsException() {
        assertThrows(MemberServiceException.class, () -> memberService.register(null));
    }

    //  LOGIN  
    @Test
    void login_Success() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Member member = new Member(1L, "john", PasswordHashUtil.hashPassword("password123", PasswordHashUtil.generateSalt()), "john@example.com");

        when(memberRepository.findByUsername("john")).thenReturn(Optional.of(member));

        MemberLoginRequestDTO loginRequest = new MemberLoginRequestDTO("john", "password123");
        MemberLoginResponseDTO response = memberService.login(loginRequest);

        assertNotNull(response);
        assertEquals("john", response.getUsername());
        assertNull(response.getToken());

        verify(memberRepository, times(1)).findByUsername("john");
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(memberRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        MemberLoginRequestDTO loginRequest = new MemberLoginRequestDTO("unknown", "password123");

        MemberServiceException ex = assertThrows(MemberServiceException.class, () -> memberService.login(loginRequest));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void login_InvalidPassword_ThrowsException() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Member member = new Member(1L, "john", PasswordHashUtil.hashPassword("password123", PasswordHashUtil.generateSalt()), "john@example.com");
        when(memberRepository.findByUsername("john")).thenReturn(Optional.of(member));

        MemberLoginRequestDTO loginRequest = new MemberLoginRequestDTO("john", "wrongpassword");

        MemberServiceException ex = assertThrows(MemberServiceException.class, () -> memberService.login(loginRequest));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    //  GET MEMBER PROFILE  
    @Test
    void getMemberProfile_Success() {
        Member member = new Member(1L, "john", "hashedpassword", "john@example.com");
        when(memberRepository.findById(String.valueOf(1L))).thenReturn(Optional.of(member));

        Optional<MemberDetailDto> profile = memberService.getMemberProfile(1L);

        assertTrue(profile.isPresent());
        assertEquals("john", profile.get().getUsername());
        assertEquals("john@example.com", profile.get().getEmail());
    }

    @Test
    void getMemberProfile_NotFound() {
        when(memberRepository.findById("1")).thenReturn(Optional.empty());

        Optional<MemberDetailDto> profile = memberService.getMemberProfile(1L);

        assertTrue(profile.isEmpty());
    }
}
